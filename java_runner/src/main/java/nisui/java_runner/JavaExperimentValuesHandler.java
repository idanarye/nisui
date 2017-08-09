package nisui.java_runner;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import nisui.core.ExperimentValuesHandler;

public class JavaExperimentValuesHandler<T> extends ExperimentValuesHandler<T> {
	private Class<T> clazz;
	private Constructor<T> constructor;
	private ArrayList<Field> fields;

	public JavaExperimentValuesHandler(Class<T> clazz) {
		this.clazz = clazz;
		try {
			this.constructor = clazz.getConstructor();
			this.constructor.setAccessible(true);
			this.fields = Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors()).filter(pd -> {
				if (pd.getReadMethod() == null || pd.getWriteMethod() == null) {
					return false;
				}
				// Make sure we don't get inherited properties here
				return clazz.isAssignableFrom(pd.getReadMethod().getDeclaringClass())
					&& clazz.isAssignableFrom(pd.getWriteMethod().getDeclaringClass());
			}).map(pd -> new JavaField(pd)).collect(Collectors.toCollection(ArrayList::new));
		} catch (IntrospectionException | NoSuchMethodException e) {
			throw new Error(e);
		}
	}

	@Override
	public String toString() {
		return String.format("Handler<%s>", clazz.getSimpleName());
	}

	@Override
	public T createValue() {
		try {
			return constructor.newInstance();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	public class JavaField extends Field {
		private PropertyDescriptor propertyDescriptor;

		private JavaField(PropertyDescriptor propertyDescriptor) {
			this.propertyDescriptor = propertyDescriptor;

			propertyDescriptor.getReadMethod().setAccessible(true);
			propertyDescriptor.getWriteMethod().setAccessible(true);
		}

		@Override
		public String getName() {
			return propertyDescriptor.getName();
		}

		@Override
		public Class getType() {
			return propertyDescriptor.getPropertyType();
		}

		@Override
		public Object get(T obj) {
			try {
				return propertyDescriptor.getReadMethod().invoke(obj);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new Error(e);
			}
		}

		@Override
		public void set(T obj, Object value) {
			try {
				propertyDescriptor.getWriteMethod().invoke(obj, value);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new Error(e);
			}
		}
	}

	@Override
	public List<ExperimentValuesHandler<T>.Field> fields() {
		return fields;
	}

	@Override
	public Field field(String name) {
		for (Field field : fields) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}
}

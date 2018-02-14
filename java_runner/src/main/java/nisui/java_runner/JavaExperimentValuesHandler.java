package nisui.java_runner;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import nisui.core.ExperimentValuesHandler;

public class JavaExperimentValuesHandler<T> extends ExperimentValuesHandler<T> {
	private Class<T> clazz;
	private LinkedHashMap<String, Field> fields;

	public JavaExperimentValuesHandler(Class<T> clazz) {
		this.clazz = clazz;
		this.fields = Arrays.stream(clazz.getDeclaredFields())
				.map(JavaField::new)
				.collect(Collectors.toMap(
							JavaField::getName,
							Function.identity(),
							(u, v) -> {
								throw new IllegalStateException(String.format("Duplicate key %s", u.getName()));
							},
							LinkedHashMap::new));
	}

	@Override
	public String toString() {
		return String.format("Handler<%s>", clazz.getSimpleName());
	}

	@Override
	public T createValue() {
		try {
			return clazz.newInstance();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	public class JavaField extends Field {
		private java.lang.reflect.Field field;

		private JavaField(java.lang.reflect.Field field) {
			this.field = field;

			field.setAccessible(true);
		}

		@Override
		public String getName() {
			return field.getName();
		}

		@Override
		public Class getType() {
			return field.getType();
		}

		@Override
		public Object get(T obj) {
			try {
				return field.get(obj);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new Error(e);
			}
		}

		@Override
		public void set(T obj, Object value) {
			try {
				field.set(obj, value);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new Error(e);
			}
		}
	}

	@Override
	public Collection<Field> fields() {
		return fields.values();
	}

	@Override
	public Field field(String name) {
		return fields.get(name);
	}
}

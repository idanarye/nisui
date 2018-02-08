package nisui.core;


import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.BiConsumer;


public abstract class ExperimentValuesHandler<T> {
	public abstract T createValue();

	public abstract class Field {
		public abstract String getName();
		public abstract Class<?> getType();
		public abstract void set(T obj, Object value);
		public abstract Object get(T obj);

		@Override
		public String toString() {
			return String.format("%s.%s", ExperimentValuesHandler.this, getName());
		}

		public Object parseString(String string) {
			if (getType().isAssignableFrom(long.class)) {
				return Long.parseLong(string);
			}
			if (getType().isAssignableFrom(int.class)) {
				return Integer.parseInt(string);
			}
			if (getType().isAssignableFrom(short.class)) {
				return Short.parseShort(string);
			}
			if (getType().isAssignableFrom(byte.class)) {
				return Byte.parseByte(string);
			}
			if (getType().isAssignableFrom(boolean.class)) {
				return Boolean.parseBoolean(string);
			}
			if (getType().isAssignableFrom(double.class)) {
				return Double.parseDouble(string);
			}
			if (getType().isAssignableFrom(float.class)) {
				return Float.parseFloat(string);
			}
			if (getType().isAssignableFrom(String.class)) {
				return string;
			}
			return null;
		}
	}
	public abstract Collection<? extends Field> fields();
	public abstract Field field(String name);

	public <S> Function<T, S> createMapper(ExperimentValuesHandler<S> other) {
		ArrayList<BiConsumer<T, S>> mappers = new ArrayList<>();
		for (Field thisField : fields()) {
			ExperimentValuesHandler<S>.Field otherField = other.field(thisField.getName());
			if (otherField != null) {
				mappers.add((t, s) -> {
					Object value = thisField.get(t);
					otherField.set(s, value);
				});
			}
		}
		return t -> {
			S s = other.createValue();
			for (BiConsumer<T, S> mapper : mappers) {
				mapper.accept(t, s);
			}
			return s;
		};
	}
}

package nisui.core;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Function;


public abstract class ExperimentValuesHandler<T> {
    public abstract T createValue();

    public T createValue(Object... args) {
        T value = createValue();
        Iterator<? extends ExperimentValuesHandler<T>.Field> iter = this.fields().iterator();
        for (Object arg : args) {
            if (iter.hasNext()) {
                ExperimentValuesHandler<T>.Field field = iter.next();
                field.set(value, arg);
            } else {
                break;
            }
        }
        return value;
    }

    public abstract class Field {
        public abstract String getName();
        public abstract Class<?> getType();
        public abstract void set(T obj, Object value);
        public abstract Object get(T obj);
        private Function<String, Object> parser;

        @Override
        public String toString() {
            return String.format("%s.%s", ExperimentValuesHandler.this, getName());
        }

        protected Function<String, Object> createParser() {
            if (getType().isAssignableFrom(long.class)) {
                return (string) -> Long.parseLong(string);
            }
            if (getType().isAssignableFrom(int.class)) {
                return (string) -> Integer.parseInt(string);
            }
            if (getType().isAssignableFrom(short.class)) {
                return (string) -> Short.parseShort(string);
            }
            if (getType().isAssignableFrom(byte.class)) {
                return (string) -> Byte.parseByte(string);
            }
            if (getType().isAssignableFrom(boolean.class)) {
                return (string) -> Boolean.parseBoolean(string);
            }
            if (getType().isAssignableFrom(double.class)) {
                return (string) -> Double.parseDouble(string);
            }
            if (getType().isAssignableFrom(float.class)) {
                return (string) -> Float.parseFloat(string);
            }
            if (getType().isAssignableFrom(String.class)) {
                return (string) -> string;
            }
            if (getType().isEnum()) {
                HashMap<String, Object> map = new HashMap<>();
                for (Object enumConstant : getType().getEnumConstants()) {
                    map.put(enumConstant.toString(), enumConstant);
                }
                return map::get;
            }
            return (string) -> null;
        }

        public Object parseString(String string) {
            if (parser == null) {
                parser = createParser();
            }
            return parser.apply(string);
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

    public String formatAsString(T value) {
        StringBuilder result = new StringBuilder();
        result.append('(');
        boolean isFirst = true;
        for (Field field : this.fields()) {
            if (isFirst) {
                isFirst = false;
            } else {
                result.append(", ");
            }
            result.append(field.getName());
            result.append('=');
            result.append(field.get(value));
        }
        result.append(')');
        return result.toString();
    }
}

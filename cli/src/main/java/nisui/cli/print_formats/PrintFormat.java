package nisui.cli.print_formats;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import nisui.core.ExperimentValuesHandler;

public abstract class PrintFormat<T> {
    protected class Field {
        public final String caption;
        public final Function<T, Object> extract;

        public Field(String caption, Function<T, Object> extract) {
            this.caption = caption;
            this.extract = extract;
        }
    }

    protected List<Field> fields;
    public PrintFormat() {
        fields = new ArrayList<>();
    }

    public abstract void printHeader(PrintStream out);
    public abstract void printValue(PrintStream out, T value);
    public abstract void printFooter(PrintStream out);

    public <V> void addFieldsFromValueHandler(ExperimentValuesHandler<V> valuesHandler, Function<T, V> mapper) {
        for (ExperimentValuesHandler<V>.Field field : valuesHandler.fields()) {
            fields.add(new Field(field.getName(), value -> field.get(mapper.apply(value))));
        }
    }
}

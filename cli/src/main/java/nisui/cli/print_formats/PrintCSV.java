package nisui.cli.print_formats;

import java.io.PrintStream;

public class PrintCSV<V> extends PrintFormat<V> {
    @Override
    public void printHeader(PrintStream out) {
        boolean isFirst = true;
        for (Field field : fields) {
            if (isFirst) {
                isFirst = false;
            } else {
                out.print(',');
            }
            out.print(field.caption);
        }
        out.println();
    }

    @Override
    public void printValue(PrintStream out, V value) {
        boolean isFirst = true;
        for (Field field : fields) {
            if (isFirst) {
                isFirst = false;
            } else {
                out.print(',');
            }
            out.print(field.extract.apply(value));
        }
        out.println();
    }

    @Override
    public void printFooter(PrintStream out) {
    }
}

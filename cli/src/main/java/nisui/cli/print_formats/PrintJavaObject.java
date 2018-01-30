package nisui.cli.print_formats;

import java.io.PrintStream;

public class PrintJavaObject<V> extends PrintFormat<V> {

    @Override
    public void printHeader(PrintStream out) {
    }

    @Override
    public void printValue(PrintStream out, V value) {
        out.println(value);
    }

    @Override
    public void printFooter(PrintStream out) {
    }
}

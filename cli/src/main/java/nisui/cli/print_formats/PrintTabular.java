package nisui.cli.print_formats;

import java.io.PrintStream;

public class PrintTabular<V> extends PrintFormat<V> {
    private int[] widths;

    private void printRepeat(PrintStream out, int times, char ch) {
        for (int i = 0; i < times; ++i) {
            out.print(ch);
        }
    }

    private void printInWidth(PrintStream out, int index, String text, char sep) {
        if (0 == index) {
            out.print(sep);
        }
        int width = widths[index];
        int fill = width - text.length();
        if (fill < 0) {
            text = text.substring(0, width);
            fill = 0;
        }
        int fillLeft = fill / 2;
        int fillRight = fill - fillLeft;
        printRepeat(out, fillLeft, ' ');
        out.print(text);
        printRepeat(out, fillRight, ' ');
        out.print(sep);
    }

    @Override
    public void printHeader(PrintStream out) {
        widths = new int[fields.size()];
        for (int i = 0; i < widths.length; ++i) {
            Field field = fields.get(i);
            widths[i] = field.caption.length() + 2;
            printInWidth(out, i, field.caption, ' ');
        }
        out.println();
        out.print('-');
        for (int width : widths) {
            printRepeat(out, width, '-');
            out.print('-');
        }
        out.println();
    }

    @Override
    public void printValue(PrintStream out, V value) {
        for (int i = 0; i < widths.length; ++i) {
            Object obj = fields.get(i).extract.apply(value);
            String str = obj == null ? "" : obj.toString();
            printInWidth(out, i, str, ' ');
        }
        out.println();
    }

    @Override
    public void printFooter(PrintStream out) {
    }
}

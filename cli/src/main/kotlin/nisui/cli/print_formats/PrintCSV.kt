package nisui.cli.print_formats

import java.io.PrintStream

public class PrintCSV<V> : PrintFormat<V>() {
    override fun printHeader(out_: PrintStream) {
        var isFirst = true
        for (field in fields) {
            if (isFirst) {
                isFirst = false
            } else {
                out_.print(',')
            }
            out_.print(field.caption)
        }
        out_.println()
    }

    override fun printValue(out_: PrintStream, value: V) {
        var isFirst = true
        for (field in fields) {
            if (isFirst) {
                isFirst = false
            } else {
                out_.print(',')
            }
            out_.print(field.extract(value))
        }
        out_.println()
    }

    override fun printFooter(out_: PrintStream) {
    }
}

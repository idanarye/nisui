package nisui.cli.print_formats

import java.io.PrintStream

public class PrintTabular<V> : PrintFormat<V>() {
    private var widths: IntArray = intArrayOf()

    private fun printRepeat(out_: PrintStream, times: Int, ch: Char) {
        repeat (times) {
            out_.print(ch)
        }
    }

    private fun printInWidth(out_: PrintStream, index: Int, text: String, sep: Char) {
        if (0 == index) {
            out_.print(sep)
        }
        val width = widths[index]
        val fill = width - text.length
        if (fill < 0) {
            out_.print(text.substring(0, width))
        } else {
            val fillLeft = fill / 2
            val fillRight = fill - fillLeft
            printRepeat(out_, fillLeft, ' ')
            out_.print(text)
            printRepeat(out_, fillRight, ' ')
        }
        out_.print(sep)
    }

    override fun printHeader(out_: PrintStream) {
        widths = IntArray(fields.size)
        for ((i, field) in fields.withIndex()) {
            widths[i] = field.caption.length + 2
            printInWidth(out_, i, field.caption, ' ')
        }
        out_.println()
        out_.print('-')
        for (width in widths) {
            printRepeat(out_, width, '-')
            out_.print('-')
        }
        out_.println()
    }

    override fun printValue(out_: PrintStream, value: V) {
        for ((i, field) in fields.withIndex()) {
            val obj = field.extract(value)
            val str = obj?.let { it.toString() } ?: ""
            printInWidth(out_, i, str, ' ')
        }
        out_.println()
    }

    override fun printFooter(out_: PrintStream) {
    }
}

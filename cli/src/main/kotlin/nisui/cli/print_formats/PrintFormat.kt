package nisui.cli.print_formats

import java.io.PrintStream
import java.util.ArrayList
import java.util.List
import java.util.function.BiConsumer
import java.util.function.Consumer

import nisui.core.ExperimentValuesHandler

public abstract class PrintFormat<T> {
    protected inner class Field (val caption: String, val extract: (T) -> Any)

    protected val fields: ArrayList<Field> = arrayListOf()

    public abstract fun printHeader(out_: PrintStream)
    public abstract fun printValue(out_: PrintStream, value: T)
    public abstract fun printFooter(out_: PrintStream)

    public fun <V> addField(caption: String, extract: (T) -> Any) {
        fields.add(Field(caption, extract))
    }

    public fun <V> addFieldsFromValueHandler(valuesHandler: ExperimentValuesHandler<V>, mapper: (T) -> V) {
        for (field in valuesHandler.fields()) {
            addField<V>(field.name, { field.get(mapper(it)) })
        }
    }
}

package nisui.cli

import java.io.InputStream
import java.io.PrintStream
import java.util.function.Supplier;

import picocli.CommandLine

import nisui.core.NisuiFactory
import nisui.core.ExperimentValuesHandler;

import nisui.cli.print_formats.PrintFormat
import nisui.cli.print_formats.PrintTabular

public abstract class CommandGroup(val nisuiFactory: NisuiFactory) : SubCommand() {
    var printFormatCreator: () -> PrintFormat<*> = { PrintTabular<Any>() }

    protected fun <T> createPrintFormat(): PrintFormat<T> = printFormatCreator() as PrintFormat<T>

    override fun run(in_: InputStream?, out_: PrintStream) {
        return
    }

    override fun register(commandLine: CommandLine): CommandLine {
        val commandGroup = super.register(commandLine)

        for (clazz in this.javaClass.getDeclaredClasses()) {
            if (SubCommand::class.java.isAssignableFrom(clazz)) {
                val subClass = clazz.asSubclass(SubCommand::class.java)
                this.registerSubcommand(commandGroup, subClass)
            }
        }

        return commandGroup
    }

    private fun <T : SubCommand> registerSubcommand(commandGroup: CommandLine, clazz: Class<T>) {
        val ctor = clazz.getDeclaredConstructor(this.javaClass)
        val subCommand = ctor.newInstance(this)
        for (name in subCommand.getNames()) {
            commandGroup.addSubcommand(name, subCommand)
        }
    }

    companion object {
        fun <V> parseValueAssignment(valuesHandler: ExperimentValuesHandler<V>, valueAssignments: Iterable<String>?): V {
            val value = valuesHandler.createValue();
            if (valueAssignments != null) {
                for (assignment in valueAssignments) {
                    val parts = assignment.split("=", limit = 2)
                    val field = valuesHandler.field(parts[0])
                    field.set(value, field.parseString(parts[1]))
                }
            }
            return value
        }
    }
}

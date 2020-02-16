package nisui.cli

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.PrintStream
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.util.Arrays
import java.util.List

import picocli.CommandLine

import nisui.cli.print_formats.*
import nisui.core.NisuiFactory

@CommandLine.Command
public class EntryPoint(val nisuiFactory: NisuiFactory) {
    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true, description = ["Show this help message and exit."])
    var helpRequested: Boolean = false

    @CommandLine.Option(names = ["--format"], description = ["The format to print the results in."])
    var format: String = ""

    fun run(in_: InputStream?, out_: PrintStream, vararg args: String) {
        val commandLine = CommandLine(this)

        val mainCommand = StartGuiSubcommand(nisuiFactory)
        mainCommand.register(commandLine)

        ExperimentSubcommand(nisuiFactory).register(commandLine)
        DataPointSubcommand(nisuiFactory).register(commandLine)
        ExperimentResultSubcommand(nisuiFactory).register(commandLine)
        RunSubcommand(nisuiFactory).register(commandLine)
        QueriesSubcommand(nisuiFactory).register(commandLine)

        val subCommands = commandLine.parseArgs(*args).asCommandLineList()

        if (helpRequested) {
            commandLine.usage(out_)
            return
        }

        var ranSomething = false

        for ((i, subCommand) in subCommands.withIndex()) {
            val obj = subCommand.getCommand<Any>()
            if (obj is CommandGroup) {
                val commandGroup: CommandGroup = obj
                if (
                        commandGroup.helpRequested
                        || i == subCommands.size - 1
                        || subCommands.get(i + 1).parent != subCommand
                   ) {
                    subCommand.usage(out_)
                    return
                }
                commandGroup.printFormatCreator = when (format) {
                    "" -> ({ PrintTabular<Any>() })
                    "csv" -> ({ PrintCSV<Any>() })
                    else -> {
                        throw Exception("Unknown format $format")
                    }
                }
            }
            if (obj is SubCommand) {
                val subCmd: SubCommand = obj
                if (subCmd.helpRequested) {
                    subCommand.usage(out_)
                    return
                }
                ranSomething = true
                subCmd.run(in_, out_)
            }
        }

        if (!ranSomething) {
            mainCommand.run(in_, out_)
        }
    }

    fun run(vararg args: String) {
        run(System.`in`, System.`out`, *args)
    }

    fun runGetOutput(vararg args: String): String {
        val baos = ByteArrayOutputStream()
        return PrintStream(baos, true, "utf-8").use { ps ->
            run(null, ps, *args)
            ps.flush()
            String(baos.toByteArray(), StandardCharsets.UTF_8)
        }
    }
}

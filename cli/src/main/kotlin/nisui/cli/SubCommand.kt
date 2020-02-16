package nisui.cli

import java.io.InputStream
import java.io.PrintStream

import picocli.CommandLine

public abstract class SubCommand {
    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true, description = ["Show this help message and exit."])
    var helpRequested: Boolean = false

    open fun register(commandLine: CommandLine): CommandLine {
        val commandGroup = CommandLine(this)
        for (name in this.getNames()) {
            commandLine.addSubcommand(name, commandGroup)
        }
        return commandGroup
    }

    public abstract fun getNames(): Array<String>
    public abstract fun run(in_: InputStream?, out_: PrintStream)
}

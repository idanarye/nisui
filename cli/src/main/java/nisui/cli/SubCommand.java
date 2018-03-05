package nisui.cli;

import java.io.InputStream;
import java.io.PrintStream;
import picocli.CommandLine;

public abstract class SubCommand {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
    boolean helpRequested;

    CommandLine register(CommandLine commandLine) {
        CommandLine commandGroup = new CommandLine(this);
        for (String name : this.getNames()) {
            commandLine.addSubcommand(name, commandGroup);
        }
        return commandGroup;
    }

    public abstract String[] getNames();
    public abstract void run(InputStream in, PrintStream out);
}

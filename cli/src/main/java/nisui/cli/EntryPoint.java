package nisui.cli;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import picocli.CommandLine;
import nisui.cli.print_formats.*;
import nisui.core.NisuiFactory;

@CommandLine.Command
public class EntryPoint {
    private NisuiFactory nisuiFactory;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
    private boolean helpRequested;

    @CommandLine.Option(names = {"--format"}, description = "The format to print the results in.")
    String format = "";

    public EntryPoint(NisuiFactory nisuiFactory) {
        this.nisuiFactory = nisuiFactory;
    }

    public void run(InputStream in, PrintStream out, String... args) {
        CommandLine commandLine = new CommandLine(this);

        new ExperimentSubcommand(nisuiFactory).register(commandLine);
        new DataPointSubcommand(nisuiFactory).register(commandLine);
        new ExperimentResultSubcommand(nisuiFactory).register(commandLine);
        new RunSubcommand(nisuiFactory).register(commandLine);

        List<CommandLine> subCommands = commandLine.parse(args);

        if (helpRequested) {
            commandLine.usage(out);
            return;
        }

        boolean ranSomething = false;

        for (int i = 0; i < subCommands.size(); ++i) {
            CommandLine subCommand = subCommands.get(i);
            Object obj = subCommand.<Object>getCommand();
            if (obj instanceof CommandGroup) {
                CommandGroup commandGroup = CommandGroup.class.cast(obj);
                if (
                        commandGroup.helpRequested
                        || i == subCommands.size() - 1
                        || subCommands.get(i + 1).getParent() != subCommand
                   ) {
                    subCommand.usage(out);
                    return;
                }
                switch (format) {
                    case "":
                        commandGroup.printFormatSupplier = PrintTabular::new;
                        break;
                    case "csv":
                        commandGroup.printFormatSupplier = PrintCSV::new;
                        break;
                }
            }
            if (obj instanceof SubCommand) {
                SubCommand subCmd = SubCommand.class.cast(obj);
                if (subCmd.helpRequested) {
                    subCommand.usage(out);
                    return;
                }
                ranSomething = true;
                subCmd.run(in, out);
            }
        }

        if (!ranSomething) {
            commandLine.usage(out);
        }
    }

    public void run(String... args) {
        run(System.in, System.out, args);
    }

    public String runGetOutput(String... args) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, "utf-8")) {
            run(null, ps, args);
            ps.flush();
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }
}

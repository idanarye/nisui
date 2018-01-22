package nisui.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import picocli.CommandLine;

import nisui.core.NisuiFactory;

@CommandLine.Command
public class EntryPoint {
	private NisuiFactory nisuiFactory;

	public EntryPoint(NisuiFactory nisuiFactory) {
		this.nisuiFactory = nisuiFactory;
	}

	public void run(String... args) {
		CommandLine commandLine = new CommandLine(this);

		new ExperimentSubcommand(nisuiFactory).register(commandLine);
		new DataPointSubcommand(nisuiFactory).register(commandLine);

		for (CommandLine subCommand : commandLine.parse(args)) {
			Object obj = subCommand.<Object>getCommand();
			if (obj instanceof SubCommand) {
				SubCommand.class.cast(obj).run(System.in, System.out);
			}
		}
	}
}

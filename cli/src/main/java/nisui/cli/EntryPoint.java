package nisui.cli;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import picocli.CommandLine;
import nisui.cli.print_formats.*;
import nisui.core.NisuiFactory;

@CommandLine.Command
public class EntryPoint {
	private NisuiFactory nisuiFactory;

	@CommandLine.Option(names = {"--format"})
	String format = "";

	public EntryPoint(NisuiFactory nisuiFactory) {
		this.nisuiFactory = nisuiFactory;
	}

	public void run(InputStream in, PrintStream out, String... args) {
		CommandLine commandLine = new CommandLine(this);

		new ExperimentSubcommand(nisuiFactory).register(commandLine);
		new DataPointSubcommand(nisuiFactory).register(commandLine);

		for (CommandLine subCommand : commandLine.parse(args)) {
			Object obj = subCommand.<Object>getCommand();
			if (obj instanceof CommandGroup) {
				CommandGroup commandGroup = CommandGroup.class.cast(obj);
				switch (format) {
					case "":
						commandGroup.printFormatSupplier = PrintJavaObject::new;
						break;
					case "csv":
						commandGroup.printFormatSupplier = PrintCSV::new;
						break;
				}
			}
			if (obj instanceof SubCommand) {
				SubCommand.class.cast(obj).run(in, out);
			}
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

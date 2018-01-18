package nisui.cli;

import picocli.CommandLine;
import java.io.InputStream;
import java.io.PrintStream;
import nisui.core.ExperimentFunction;
import nisui.core.ExperimentValuesHandler;
import nisui.core.NisuiFactory;

@CommandLine.Command
public class ExperimentSubcommand {
	private NisuiFactory nisuiFactory;

	void register(CommandLine commandLine) {
		commandLine.addSubcommand("experiment",
				new CommandLine(this)
				.addSubcommand("info", new Info())
				);
	}

	public ExperimentSubcommand(NisuiFactory nisuiFactory) {
		this.nisuiFactory = nisuiFactory;
	}

	@CommandLine.Command
	class Info implements SubCommand {
		@Override
		public void run(InputStream in, PrintStream out) {
			ExperimentFunction<?, ?> experimentFunction = nisuiFactory.createExperimentFunction();
			printHandler("Data Points", experimentFunction.getDataPointHandler(), out);
			printHandler("Experiment Results", experimentFunction.getExperimentResultHandler(), out);
		}

		private void printHandler(String caption, ExperimentValuesHandler<?> handler, PrintStream out) {
			out.printf("%s:\n", caption);
			for (ExperimentValuesHandler.Field field : handler.fields()) {
				out.printf("    %s: %s", field.getName(), field.getType().getSimpleName());
				Object[] enumConstants = field.getType().getEnumConstants();
				if (enumConstants != null) {
					out.print('(');
					for (int i = 0; i < enumConstants.length; ++i) {
						if (0 < i) {
							out.print('|');
						}
						out.print(enumConstants[i]);
					}
					out.print(')');
				}
				out.println();
			}
		}
	}
}

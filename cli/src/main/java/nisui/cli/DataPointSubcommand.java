package nisui.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import nisui.core.ExperimentFunction;
import nisui.core.ExperimentValuesHandler;
import nisui.core.NisuiFactory;

@CommandLine.Command
public class DataPointSubcommand {
    private static Logger logger = LoggerFactory.getLogger(DataPointSubcommand.class);

    private NisuiFactory nisuiFactory;

    void register(CommandLine commandLine) {
        CommandLine subCommand = new CommandLine(this)
            .addSubcommand("add", new Add());
        commandLine.addSubcommand("data-points", subCommand);
        commandLine.addSubcommand("dp", subCommand);
    }

    public DataPointSubcommand(NisuiFactory nisuiFactory) {
        this.nisuiFactory = nisuiFactory;
    }

    @CommandLine.Command
    class Add implements SubCommand {
		@Override
		public String[] getNames() {
			return new String[]{"add"};
		}

        @Override
        public void run(InputStream in, PrintStream out) {
        }
    }
}

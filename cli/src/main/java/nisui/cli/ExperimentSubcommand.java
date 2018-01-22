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
public class ExperimentSubcommand {
    private static Logger logger = LoggerFactory.getLogger(ExperimentSubcommand.class);

    private NisuiFactory nisuiFactory;

    void register(CommandLine commandLine) {
        commandLine.addSubcommand("experiment",
                new CommandLine(this)
                .addSubcommand("info", new Info())
                .addSubcommand("run", new Run())
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

    @CommandLine.Command
    class Run implements SubCommand {
        @CommandLine.Option(names = {"-s", "--seed"})
        long seed = 0;

        @CommandLine.Parameters
        List<String> dataPointValues;

        @Override
        @SuppressWarnings("unchecked")
        public void run(InputStream in, PrintStream out) {
            run(out, nisuiFactory.createExperimentFunction());
        }

        public <D, R> void run(PrintStream out, ExperimentFunction<D, R> experimentFunction) {
            ExperimentValuesHandler<D> dataPointHandler = experimentFunction.getDataPointHandler();
            D dataPoint = dataPointHandler.createValue();
            for (String dataPointValue : dataPointValues) {
                String[] parts = dataPointValue.split("=", 2);
                ExperimentValuesHandler<D>.Field field = dataPointHandler.field(parts[0]);
                field.set(dataPoint, field.parseString(parts[1]));
            }

            long seed = this.seed;
            if (seed == 0) {
                seed = System.currentTimeMillis();
                logger.info("Picking seed = {}", seed);
            }
            R experimentResult = experimentFunction.runExperiment(dataPoint, seed);

            for (ExperimentValuesHandler<R>.Field field : experimentFunction.getExperimentResultHandler().fields()) {
                out.printf("%s: %s\n", field.getName(), field.get(experimentResult));
            }
        }
    }
}

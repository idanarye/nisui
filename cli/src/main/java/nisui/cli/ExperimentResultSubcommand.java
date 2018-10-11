package nisui.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import nisui.cli.print_formats.PrintCSV;
import nisui.cli.print_formats.PrintFormat;
import nisui.core.*;

@CommandLine.Command(
name = "experiment-results",
description = "Commands for dealing with the results of the experiments we ran.")
public class ExperimentResultSubcommand extends CommandGroup {
    private static Logger logger = LoggerFactory.getLogger(ExperimentResultSubcommand.class);

    @Override
    public String[] getNames() {
        return new String[]{"experiment-results", "er"};
    }

    public ExperimentResultSubcommand(NisuiFactory nisuiFactory) {
        super(nisuiFactory);
    }

    @CommandLine.Command(
    name = "list",
    description = "Print the results of the experiments we ran.")
    class List_ extends SubCommand {
        @Override
        public String[] getNames() {
            return new String[]{"list"};
        }

        @Override
        public void run(InputStream in, PrintStream out) {
            ResultsStorage<?, ?> storage;
            try {
                storage = nisuiFactory.createResultsStorage();
            } catch (ExperimentFunctionCreationException e) {
                throw new ExitException(e.getMessage());
            }
            run(out, storage);
        }

        private <D, R> ExperimentResultsReader<D, R> createReader(ResultsStorage<D, R>.Connection con) throws Exception {
            try (DataPointsReader<D> dpReader = con.readDataPoints()) {
                return con.readExperimentResults(dpReader);
            }
        }

        private <D, R> void run(PrintStream out, ResultsStorage<D, R> storage) {
            PrintFormat<ExperimentResult<D, R>> printFormat = createPrintFormat();
            printFormat.addField("datapoint_key", experimentResult -> experimentResult.getDataPoint().getKey());
            printFormat.addFieldsFromValueHandler(storage.getDataPointHandler(), experimentResult -> experimentResult.getDataPoint().getValue());
            printFormat.addField("seed", experimentResult -> experimentResult.getSeed());
            printFormat.addFieldsFromValueHandler(storage.getExperimentResultHandler(), experimentResult -> experimentResult.getValue());
            try (ResultsStorage<D, R>.Connection con = storage.connect()) {
                try (ExperimentResultsReader<D, R> reader = createReader(con)) {
                    printFormat.printHeader(out);
                    for (ExperimentResult<D, R> experimentResult : reader) {
                        printFormat.printValue(out, experimentResult);
                    }
                    printFormat.printFooter(out);
                }
            } catch (Exception e) {
                logger.error("Cant connect to database - {}", e);
            }
        }
    }
}

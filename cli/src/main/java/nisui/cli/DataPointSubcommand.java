package nisui.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import nisui.cli.print_formats.PrintCSV;
import nisui.cli.print_formats.PrintFormat;
import nisui.core.DataPoint;
import nisui.core.DataPointInserter;
import nisui.core.DataPointsReader;
import nisui.core.ExperimentFunction;
import nisui.core.ExperimentResultInserter;
import nisui.core.ExperimentValuesHandler;
import nisui.core.NisuiFactory;
import nisui.core.ResultsStorage;
import nisui.core.ExperimentFunctionCreationException;

@CommandLine.Command(
name = "data-points",
description = "Commands for dealing with the list of data-points we want to run.")
public class DataPointSubcommand extends CommandGroup {
    private static Logger logger = LoggerFactory.getLogger(DataPointSubcommand.class);

    public DataPointSubcommand(NisuiFactory nisuiFactory) {
        super(nisuiFactory);
    }

    @Override
    public String[] getNames() {
        return new String[]{"data-points", "dp"};
    }

    @CommandLine.Command(
    name = "add",
    description = "Add a data-point to be run later (with the `run` command)")
    class Add extends SubCommand {
        @Override
        public String[] getNames() {
            return new String[]{"add"};
        }

        @CommandLine.Option(names = {"-n", "--num-planned"}, required = true, description = "The number of experiments to run on this data-point.")
        long numPlanned;

        @CommandLine.Parameters(arity = "0..*", paramLabel = "<name>=<value>", description = "Data-point fields.")
        List<String> dataPointValues;

        @Override
        public void run(InputStream in, PrintStream out) {
            ResultsStorage<?, ?> storage;
            try {
                storage = getNisuiFactory().createResultsStorage();
            } catch (ExperimentFunctionCreationException e) {
                throw new ExitException(e.getMessage());
            }
            run(out, storage);
        }

        private <D> void run(PrintStream out, ResultsStorage<D, ?> storage) {
            storage.prepareStorage();
            D dataPoint = Companion.parseValueAssignment(storage.getDataPointHandler(), dataPointValues);
            try (ResultsStorage<D, ?>.Connection con = storage.connect()) {
                try (DataPointInserter<D> inserter = con.insertDataPoints()) {
                    inserter.insert(numPlanned, 0, dataPoint);
                }
            } catch (Exception e) {
                logger.error("Cant connect to database - {}", e);
            }
        }
    }

    @CommandLine.Command(
    name = "list",
    description = "Print the data-points that we want to run.")
    class List_ extends SubCommand {
        @Override
        public String[] getNames() {
            return new String[]{"list"};
        }

        @Override
        public void run(InputStream in, PrintStream out) {
            ResultsStorage<?, ?> storage;
            try {
                storage = getNisuiFactory().createResultsStorage();
            } catch (ExperimentFunctionCreationException e) {
                throw new ExitException(e.getMessage());
            }
            run(out, storage);
        }

        private <D> void run(PrintStream out, ResultsStorage<D, ?> storage) {
            PrintFormat<DataPoint<D>> printFormat = createPrintFormat();
            printFormat.addField("key", dataPoint -> dataPoint.getKey());
            printFormat.addField("num_planned", dataPoint -> dataPoint.getNumPlanned());
            printFormat.addField("num_performed", dataPoint -> dataPoint.getNumPerformed());
            printFormat.addFieldsFromValueHandler(storage.getDataPointHandler(), value -> value.getValue());
            try (ResultsStorage<D, ?>.Connection con = storage.connect()) {
                try (DataPointsReader<D> reader = con.readDataPoints()) {
                    printFormat.printHeader(out);
                    for (DataPoint<D> dataPoint : reader) {
                        printFormat.printValue(out, dataPoint);
                    }
                    printFormat.printFooter(out);
                }
            } catch (Exception e) {
                logger.error("Cant connect to database - {}", e);
            }
        }
    }
}

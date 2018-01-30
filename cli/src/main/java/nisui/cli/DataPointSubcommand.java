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

@CommandLine.Command
public class DataPointSubcommand extends CommandGroup {
    private static Logger logger = LoggerFactory.getLogger(DataPointSubcommand.class);

    public DataPointSubcommand(NisuiFactory nisuiFactory) {
        super(nisuiFactory, "data-points", "dp");
    }

    @CommandLine.Command
    class Add implements SubCommand {
        @Override
        public String[] getNames() {
            return new String[]{"add"};
        }

        @CommandLine.Parameters
        List<String> dataPointValues;

        @Override
        public void run(InputStream in, PrintStream out) {
            ResultsStorage<?, ?> storage = nisuiFactory.createResultsStorage();
            run(out, storage);
        }

        private <D> void run(PrintStream out, ResultsStorage<D, ?> storage) {
            storage.prepareStorage();
            D dataPoint = parseValueAssignment(storage.getDataPointHandler(), dataPointValues);
            try (ResultsStorage<D, ?>.Connection con = storage.connect()) {
                try (DataPointInserter<D> inserter = con.insertDataPoints()) {
                    inserter.insert(dataPoint);
                }
            } catch (Exception e) {
                logger.error("Cant connect to database - {}", e);
            }
        }
    }

    @CommandLine.Command
    class List_ implements SubCommand {
        @Override
        public String[] getNames() {
            return new String[]{"list"};
        }

        @Override
        public void run(InputStream in, PrintStream out) {
            ResultsStorage<?, ?> storage = nisuiFactory.createResultsStorage();
            run(out, storage);
        }

        private <D> void run(PrintStream out, ResultsStorage<D, ?> storage) {
            PrintFormat<DataPoint<D>> printFormat = createPrintFormat();
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

package nisui.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import nisui.cli.print_formats.PrintFormat;
import nisui.core.DataPointsReader;
import nisui.core.ExperimentValuesHandler;
import nisui.core.NisuiFactory;
import nisui.core.ResultsStorage;
import nisui.core.util.SortedByAppereanceIn;
import nisui.core.QueryRunner;
import nisui.core.ExperimentFunctionCreationException;

@CommandLine.Command(
name = "queries",
description = "Commands for running queries on experiment results.")
public class QueriesSubcommand extends CommandGroup {
    private static Logger logger = LoggerFactory.getLogger(QueriesSubcommand.class);

    @Override
    public String[] getNames() {
        return new String[]{"queries", "query", "q"};
    }

    public QueriesSubcommand(NisuiFactory nisuiFactory) {
        super(nisuiFactory);
    }

    @CommandLine.Command(
    name = "run",
    description = "Run a query.")
    class Run extends SubCommand {
        @Override
        public String[] getNames() {
            return new String[]{"run"};
        }

        @CommandLine.Parameters(arity = "1..*", paramLabel = "<expr>", description = "Query Expressions")
        String[] queries;

        @CommandLine.Option(names = {"-b", "--by"}, paramLabel = "<field>", required = false, description = "Data Point fields to group-sort by")
        String[] by;

        @CommandLine.Option(names = {"-f", "--filter"}, paramLabel = "<pred>", required = false, description = "Filter Expressions")
        String[] filters;

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

        private <D, R> QueryRunner<D> createRunner(ResultsStorage<D, R>.Connection con) throws Exception {
            if (filters == null) {
                filters = new String[0];
            }
            try (DataPointsReader<D> dpReader = con.readDataPoints(filters)) {
                return con.runQuery(dpReader, queries, by);
            }
        }

        private <D, R> void run(PrintStream out, ResultsStorage<D, R> storage) {
            if (by == null) {
                by = new String[0];
            }

            try (ResultsStorage<D, R>.Connection con = storage.connect()) {
                try (QueryRunner<D> queryRunner = createRunner(con)) {
                    PrintFormat<QueryRunner.Row<D>> printFormat = createPrintFormat(storage.getDataPointHandler());
                    printFormat.printHeader(out);
                    for (QueryRunner.Row<D> row : queryRunner) {
                        printFormat.printValue(out, row);
                    }
                    printFormat.printFooter(out);
                }
            } catch (Exception e) {
                logger.error("Cant connect to database - {}", e);
            }
        }

        private <D> PrintFormat<QueryRunner.Row<D>> createPrintFormat(ExperimentValuesHandler<D> dpHandler) {
            PrintFormat<QueryRunner.Row<D>> printFormat = QueriesSubcommand.this.createPrintFormat();
            logger.info("Sorting by {} first", Arrays.asList(by));
            SortedByAppereanceIn.sortedByIndexIn(
                        dpHandler.fields().stream().map(a -> (ExperimentValuesHandler<D>.Field)a),
                        Arrays.stream(by).map(dpHandler::field),
                        false, false).forEach(field -> {
                printFormat.addField(field.getName(), row -> field.get(row.dataPoint));
            });
            for (int i = 0; i < queries.length; ++i) {
                int ii = i;
                printFormat.addField(queries[i], row -> row.values[ii]);
            }
            return printFormat;
        }
    }
}

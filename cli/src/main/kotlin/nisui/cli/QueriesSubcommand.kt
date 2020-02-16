package nisui.cli

import java.io.InputStream
import java.io.PrintStream

import kotlin.streams.asSequence

import org.slf4j.LoggerFactory;

import picocli.CommandLine

import nisui.core.DataPointsReader;
import nisui.core.ExperimentValuesHandler;
import nisui.core.NisuiFactory;
import nisui.core.ResultsStorage;
import nisui.core.util.SortedByAppereanceIn;
import nisui.core.QueryRunner;
import nisui.core.ExperimentFunctionCreationException;

import nisui.cli.print_formats.PrintFormat;

@CommandLine.Command(
    name = "queries",
    description = ["Commands for running queries on experiment results."]
)
public class QueriesSubcommand(nisuiFactory: NisuiFactory) : CommandGroup(nisuiFactory) {
    companion object {
        private val logger = LoggerFactory.getLogger(this.javaClass.declaringClass)
    }

    override fun getNames(): Array<String> {
        return arrayOf("queries", "query", "q")
    }

    @CommandLine.Command(
        name = "run",
        description = ["Run a query."]
    )
    inner class Run : SubCommand() {
        override fun getNames(): Array<String> {
            return arrayOf("run")
        }

        @CommandLine.Parameters(arity = "1..*", paramLabel = "<expr>", description = ["Query Expressions"])
        var queries: Array<String> = emptyArray()

        @CommandLine.Option(names = ["-b", "--by"], paramLabel = "<field>", required = false, description = ["Data Point fields to group-sort by"])
        var by_: Array<String> = emptyArray()

        @CommandLine.Option(names = ["-f", "--filter"], paramLabel = "<pred>", required = false, description = ["Filter Expressions"])
        var filters: ArrayList<String> = arrayListOf()

        override fun run(in_: InputStream?, out_: PrintStream) {
            run(out_, nisuiFactory.createResultsStorage())
        }

        private fun <D, R> run(out_: PrintStream, storage: ResultsStorage<D, R>) {
            storage.connect().use { con ->
                con.readDataPoints(filters).use { con.runQuery(it, queries, by_) }.use { queryRunner ->
                    val printFormat = createPrintFormat(storage.getDataPointHandler())
                    printFormat.printHeader(out_);
                    for (row in queryRunner) {
                        printFormat.printValue(out_, row);
                    }
                    printFormat.printFooter(out_);
                }
            }
        }

        private fun <D> createPrintFormat(dpHandler: ExperimentValuesHandler<D>): PrintFormat<QueryRunner.Row<D>>  {
            val printFormat = this@QueriesSubcommand.createPrintFormat<QueryRunner.Row<D>>()
            logger.info("Sorting by {} first", by_)
            for (field in SortedByAppereanceIn.sortedByIndexIn(
                dpHandler.fields().map{it as ExperimentValuesHandler<D>.Field}.stream(),
                by_.map(dpHandler::field).stream(),
                false, false).asSequence()
            ) {
                printFormat.addField<Any>(field.getName(), { row -> field.get(row.dataPoint) });
            }
            queries.forEachIndexed { i, query ->
                printFormat.addField<Any>(query, { row -> row.values[i] });
            }
            return printFormat;
        }
    }
}

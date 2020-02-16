package nisui.cli

import java.io.InputStream
import java.io.PrintStream

import org.slf4j.LoggerFactory

import picocli.CommandLine

import nisui.core.DataPoint
import nisui.core.DataPointInserter
import nisui.core.DataPointsReader
import nisui.core.ExperimentFunction
import nisui.core.ExperimentResultInserter
import nisui.core.ExperimentValuesHandler
import nisui.core.NisuiFactory
import nisui.core.ResultsStorage
import nisui.core.ExperimentFunctionCreationException

import nisui.cli.print_formats.PrintCSV
import nisui.cli.print_formats.PrintFormat

@CommandLine.Command(
    name = "data-points",
    description = ["Commands for dealing with the list of data-points we want to run."]
)
public class DataPointSubcommand(nisuiFactory: NisuiFactory) : CommandGroup(nisuiFactory) {
    companion object {
        private val logger = LoggerFactory.getLogger(DataPointSubcommand::class.java)
    }

    override fun getNames(): Array<String> {
        return arrayOf("data-points", "dp")
    }

    @CommandLine.Command(
        name = "add",
        description = ["Add a data-point to be run later (with the `run` command)"]
    )
    inner class Add : SubCommand() {
        override fun getNames(): Array<String> {
            return arrayOf("add")
        }

        @CommandLine.Option(
            names = ["-n", "--num-planned"],
            required = true,
            description = ["The number of experiments to run on this data-point."]
        )
        var numPlanned : Long = 0;

        @CommandLine.Parameters(
            arity = "0..*",
            paramLabel = "<name>=<value>",
            description = ["Data-point fields."]
        )
        var dataPointValues: Array<String> = emptyArray();

        override fun run(in_: InputStream?, out_: PrintStream) {
            run(out_, nisuiFactory.createResultsStorage())
        }

        private fun <D, R> run(out_: PrintStream, storage: ResultsStorage<D, R>) {
            storage.prepareStorage();
            val dataPoint = parseValueAssignment(storage.getDataPointHandler(), dataPointValues.asList())
            storage.connect().use { con ->
                con.insertDataPoints().use { inserter ->
                    inserter.insert(numPlanned, 0, dataPoint)
                }
            }
        }
    }

    @CommandLine.Command(
        name = "list",
        description = ["Print the data-points that we want to run."]
    )

    inner class List_ : SubCommand() {
        override fun getNames(): Array<String> {
            return arrayOf("list")
        }

        override fun run(in_: InputStream?, out_: PrintStream) {
            run(out_, nisuiFactory.createResultsStorage())
        }

        private fun <D, R> run(out_: PrintStream, storage: ResultsStorage<D, R>) {
            val printFormat = createPrintFormat<DataPoint<D>>()
            printFormat.addField<Any>("key", { it.getKey() })
            printFormat.addField<Any>("num_planned", { it.getNumPlanned() })
            printFormat.addField<Any>("num_performed", { it.getNumPerformed() })
            printFormat.addFieldsFromValueHandler(storage.getDataPointHandler(), { it.getValue() })
            storage.connect().use { con ->
                con.readDataPoints().use { reader ->
                    printFormat.printHeader(out_)
                    for (dataPoint in reader) {
                        printFormat.printValue(out_, dataPoint)
                    }
                    printFormat.printFooter(out_)
                }
            }
        }
    }
}

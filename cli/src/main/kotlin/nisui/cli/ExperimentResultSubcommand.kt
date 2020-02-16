package nisui.cli

import java.io.InputStream
import java.io.PrintStream

import org.slf4j.LoggerFactory

import picocli.CommandLine

import nisui.core.*

import nisui.cli.print_formats.PrintCSV
import nisui.cli.print_formats.PrintFormat

@CommandLine.Command(
    name = "experiment-results",
    description = ["Commands for dealing with the results of the experiments we ran."]
)
public class ExperimentResultSubcommand(nisuiFactory: NisuiFactory) : CommandGroup(nisuiFactory) {
    companion object {
        private val logger = LoggerFactory.getLogger(this.javaClass.declaringClass)
    }

    override fun getNames(): Array<String> {
        return arrayOf("experiment-results", "er")
    }

    @CommandLine.Command(
        name = "list",
        description = ["Print the results of the experiments we ran."]
    )
    inner class List_ : SubCommand() {
        override fun getNames(): Array<String> = arrayOf("list")

        override fun run(in_: InputStream?, out_: PrintStream) {
            run(out_, nisuiFactory.createResultsStorage())
        }

        private fun <D, R> run(out_: PrintStream, storage: ResultsStorage<D, R>) {
            val printFormat = createPrintFormat<ExperimentResult<D, R>>()
            printFormat.addField<Any>("datapoint_key", { it.getDataPoint().getKey() })
            printFormat.addFieldsFromValueHandler(storage.getDataPointHandler(), { it.getDataPoint().getValue() })
            printFormat.addField<Any>("seed", { it.getSeed() })
            printFormat.addFieldsFromValueHandler(storage.getExperimentResultHandler(), { it.getValue() })
            storage.connect().use { con ->
                con.readDataPoints().use { con.readExperimentResults(it) } .use { reader ->
                    printFormat.printHeader(out_)
                    for (experimentResult in reader) {
                        printFormat.printValue(out_, experimentResult)
                    }
                    printFormat.printFooter(out_)
                }
            }
        }
    }
}

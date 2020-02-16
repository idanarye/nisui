package nisui.cli

import org.slf4j.LoggerFactory

import java.io.InputStream
import java.io.PrintStream

import picocli.CommandLine

import nisui.core.ExperimentFunction
import nisui.core.ExperimentValuesHandler
import nisui.core.NisuiFactory
import nisui.core.ExperimentFunctionCreationException

@CommandLine.Command(
    name = "experiment",
    description = ["Commands for dealing with the experminent runner, regardless of saved data-points or results."]
)
public class ExperimentSubcommand(nisuiFactory: NisuiFactory) : CommandGroup(nisuiFactory) {
    companion object {
        private val logger = LoggerFactory.getLogger(this.javaClass.declaringClass)
    }

    override fun getNames(): Array<String> {
        return arrayOf("experiment", "e")
    }

    @CommandLine.Command(
        name = "info",
        description = ["Print static information (data-point and result layout) on the experiment."]
    )
    inner class Info : SubCommand() {
        override fun getNames(): Array<String> {
            return arrayOf("info")
        }

        override fun run(in_: InputStream?, out_: PrintStream) {
            val experimentFunction = nisuiFactory.createExperimentFunction()
            printHandler("Data Points", experimentFunction.getDataPointHandler(), out_)
            printHandler("Experiment Results", experimentFunction.getExperimentResultHandler(), out_)
        }

        private fun printHandler(caption: String, handler: ExperimentValuesHandler<Any>, out_: PrintStream) {
            out_.printf("%s:\n", caption)
            for (field in handler.fields()) {
                out_.printf("    %s: %s", field.name, field.type.simpleName)
                val enumConstants = field.getType().getEnumConstants()
                if (enumConstants != null) {
                    out_.print('(')
                    enumConstants.forEachIndexed { i, enumConstant ->
                        if (0 < i) {
                            out_.print('|')
                        }
                        out_.print(enumConstant)
                    }
                    out_.print(')')
                }
                out_.println()
            }
        }
    }

    @CommandLine.Command(
        name = "run",
        description = ["Run the experiment once."]
    )
    inner class Run : SubCommand() {
        override fun getNames(): Array<String> {
            return arrayOf("run")
        }

        @CommandLine.Option(names = ["-s", "--seed"], description = ["Run the experiment with a specific seed. Leave out for random seed."])
        var seed: Long = 0

        @CommandLine.Parameters(arity = "0..*", paramLabel = "<name>=<value>", description = ["Data-point fields."])
        var dataPointValues: ArrayList<String> = arrayListOf()

        override fun run(in_: InputStream?, out_: PrintStream) {
            run(out_, nisuiFactory.createExperimentFunction())
        }

        private fun <D, R> run(out_: PrintStream, experimentFunction: ExperimentFunction<D, R>) {
            val dataPointHandler = experimentFunction.getDataPointHandler()
            val dataPoint = parseValueAssignment(dataPointHandler, dataPointValues)

            val seed = if (this.seed == 0.toLong()) {
                val seed = System.currentTimeMillis()
                logger.info("Picking seed = {}", seed)
                seed
            } else {
                this.seed
            }
            val experimentResult = experimentFunction.runExperiment(dataPoint, seed)

            for (field in experimentFunction.getExperimentResultHandler().fields()) {
                out_.printf("%s: %s\n", field.getName(), field.get(experimentResult))
            }
        }
    }
}

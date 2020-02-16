package nisui.cli

import java.io.PrintStream
import java.io.InputStream

import org.slf4j.LoggerFactory

import picocli.CommandLine

import nisui.core.*
import nisui.simple_reactor.ExperimentFailedException
import nisui.simple_reactor.SimpleReactor

@CommandLine.Command(
    name = "run",
    description = ["Run the data-points we have prepared, storing the results in the database."]
)
public class RunSubcommand(val nisuiFactory: NisuiFactory) : SubCommand() {
    companion object {
        private val logger = LoggerFactory.getLogger(this.javaClass.declaringClass)
    }

    override fun getNames(): Array<String> = arrayOf("run")

    @CommandLine.Option(names = ["-t", "--threads"], required = false, description = ["Number of threads to use."])
    var numThreads: Int = 0

    override fun run(in_: InputStream?, out_: PrintStream) {
        run(out_, nisuiFactory.createResultsStorage())
    }

    private fun <D, R> run(out_: PrintStream, storage: ResultsStorage<D, R>) {
        val experimentFunction = nisuiFactory.createExperimentFunction()

        val numThreads = if (numThreads == 0) {
            numThreads = Runtime.getRuntime().availableProcessors()
            logger.info("Running experiment in {} threads", numThreads)
            numThreads
        } else {
            this.numThreads
        }
        val reactor = SimpleReactor<D, R>(numThreads, storage, experimentFunction, { logger.error(it.message) })
        try {
            reactor.run()
        } catch (e: InterruptedException) {
            logger.info("Inetrrupted")
        }
    }
}

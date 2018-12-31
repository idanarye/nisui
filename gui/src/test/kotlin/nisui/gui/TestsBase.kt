package nisui.gui

import java.io.File

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import nisui.core.*
import nisui.h2_store.H2ResultsStorage
import nisui.java_runner.JavaExperimentFunction

abstract class TestsBase {
    @Rule
    fun _dbFolder() = TemporaryFolder()
    val dbFolder = _dbFolder()

    fun tmpDbFileName() = File(dbFolder.getRoot(), "test-db").getAbsolutePath()

    protected val factory = TestsFactory()

    protected inner class TestsFactory: NisuiFactory {
        val runner: JavaExperimentFunction<Any?, Any?>

        init {
            val url = javaClass.getClassLoader().getResource("DiceRoller.java")
            runner = JavaExperimentFunction.load(url.toURI())
        }
        override fun createExperimentFunction(): ExperimentFunction<*, *>? = null

        override fun createResultsStorage() = H2ResultsStorage(tmpDbFileName(), createExperimentFunction()?.getDataPointHandler(), createExperimentFunction()?.getExperimentResultHandler())
    }
}

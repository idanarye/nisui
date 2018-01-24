package nisui.cli;

import java.io.File;
import java.util.function.BiFunction;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import nisui.core.*;
import nisui.java_runner.JavaExperimentValuesHandler;
import nisui.java_runner.JavaExperimentFunction;;

public abstract class TestsBase {
	@Rule
	public TemporaryFolder dbFolder = new TemporaryFolder();

	protected String tmpDbFileName() {
		return new File(dbFolder.getRoot(), "test-db").getAbsolutePath();
	}

	public EntryPoint getEntryPoint(JavaExperimentFunction experimentFunction) {
		return new EntryPoint(new TestsFactory(experimentFunction));
	}

	private class TestsFactory implements NisuiFactory {
		private ExperimentFunction testExperimentFunction;

		public TestsFactory(ExperimentFunction testExperimentFunction) {
			this.testExperimentFunction = testExperimentFunction;
		}

		@Override
		public ExperimentFunction createExperimentFunction() {
			return testExperimentFunction;
		}

		@Override
		public ResultsStorage createResultsStorage() {
			return null;
		}
	}
}

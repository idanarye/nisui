package nisui.cli;

import java.io.File;
import java.util.function.BiFunction;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import nisui.core.*;
import nisui.java_runner.JavaExperimentValuesHandler;
import nisui.java_runner.JavaExperimentFunction;
import nisui.h2_store.H2ResultsStorage;

public abstract class TestsBase {
	@Rule
	public TemporaryFolder dbFolder = new TemporaryFolder();

	protected String tmpDbFileName() {
		return new File(dbFolder.getRoot(), "test-db").getAbsolutePath();
	}

	public EntryPoint getEntryPoint(Object experimentFunction) {
		return new EntryPoint(new TestsFactory(new JavaExperimentFunction(experimentFunction)));
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
		@SuppressWarnings("unchecked")
		public ResultsStorage createResultsStorage() {
			return new H2ResultsStorage<>(tmpDbFileName(), testExperimentFunction.getDataPointHandler(), testExperimentFunction.getExperimentResultHandler());
		}
	}
}

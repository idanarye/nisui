import nisui.java_runner.JavaExperimentFunction;

public class SimpleNisuiRunner extends JavaExperimentFunction<SimpleNisuiRunner.DataPoint, SimpleNisuiRunner.ExperimentResult> {
	public static class DataPoint {
		public long a;
	}

	public static class ExperimentResult {
		public long x;
	}

	@Override
	public ExperimentResult runExperiment(DataPoint dataPoint, long seed) {
		ExperimentResult result = new ExperimentResult();
		result.x = 100 * seed + dataPoint.a;
		return result;
	}
}

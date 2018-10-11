public class SimpleNisuiRunner {
	public static class DataPoint {
		public long a;
	}

	public static class ExperimentResult {
		public long x;
	}

	public ExperimentResult runExperiment(DataPoint dataPoint, long seed) {
		ExperimentResult result = new ExperimentResult();
		result.x = 100 * seed + dataPoint.a;
		return result;
	}
}

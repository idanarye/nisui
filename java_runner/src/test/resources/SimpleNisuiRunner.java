import nisui.java_runner.JavaExperimentFunction;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
class DataPoint {
	private long a;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ExperimentResult {
	private long x;
}

public class SimpleNisuiRunner extends JavaExperimentFunction<DataPoint, ExperimentResult> {
	@Override
	public ExperimentResult runExperiment(DataPoint dataPoint, long seed) {
		return new ExperimentResult(100 * seed + dataPoint.getA());
	}
}

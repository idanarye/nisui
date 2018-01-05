import nisui.java_runner.JavaExperimentFunction;

import java.util.Random;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
class DataPoint {
	private int minValue;
	private int maxValue;
	private int numDice;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ExperimentResult {
	private int totalResult;
}

public class DiceRoller extends JavaExperimentFunction<DataPoint, ExperimentResult> {
	@Override
	public ExperimentResult runExperiment(DataPoint dataPoint, long seed) {
		Random random = new Random(seed);
		int offset = dataPoint.getMinValue();
		int rangeSize = dataPoint.getMaxValue();
		int total = 0;
		for (int i = 0; i < dataPoint.getNumDice; ++ i) {
			total += offset + random.nextInt(rangeSize);
		}
		return new ExperimentResult(total);
	}
}

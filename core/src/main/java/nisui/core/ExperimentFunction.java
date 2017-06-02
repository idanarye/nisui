package nisui.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ExperimentFunction<D extends DataPoint, R extends ExperimentResult> {
	public R runExperiment(D dataPoint, long seed);
}


package nisui.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ExperimentFunction<D, R> {
    public R runExperiment(D dataPoint, long seed);
    public ExperimentValuesHandler<D> getDataPointHandler();
    public ExperimentValuesHandler<R> getExperimentResultHandler();
}


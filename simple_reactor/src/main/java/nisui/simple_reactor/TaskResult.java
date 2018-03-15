package nisui.simple_reactor;

import nisui.core.*;

class TaskResult<D, R> {
    DataPoint<D> dataPoint;
    long seed;
    R experimentResult;

    public TaskResult(DataPoint<D> dataPoint, long seed, R experimentResult) {
        this.dataPoint = dataPoint;
        this.seed = seed;
        this.experimentResult = experimentResult;
    }

}

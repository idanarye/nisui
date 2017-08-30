package nisui.h2_store;

import nisui.core.DataPoint;
import nisui.core.ExperimentResult;

public class H2ExperimentResult<D, R> extends ExperimentResult<D, R> {
    private long id;
    private H2DataPoint<D> dataPoint;
    private long seed;
    private R value;

    public H2ExperimentResult(long id, H2DataPoint<D> dataPoint, long seed, R value) {
        this.id = id;
        this.dataPoint = dataPoint;
        this.seed = seed;
        this.value = value;
    }

    public long getId() {
        return id;
    }

	public DataPoint<D> getDataPoint() {
        return dataPoint;
    }

	public long getSeed() {
        return seed;
    }

	public R getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s|%s|%s:%s", id, dataPoint.getId(), seed, value);
    }
}

package nisui.core;

public abstract class ExperimentResult<D, R> {
	public abstract DataPoint<D> getDataPoint();
	public abstract long getSeed();
	public abstract R getValue();
}

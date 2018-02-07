package nisui.core;

/**
 * Represents an experiment that Nisui ran.
 */
public abstract class ExperimentResult<D, R> {
	/**
	 * @return the data point the experiment was ran with
	 */
	public abstract DataPoint<D> getDataPoint();
	/**
	 * @return the seed the experiment was ran with
	 */
	public abstract long getSeed();
	/**
	 * @return the result of the experiment
	 */
	public abstract R getValue();
}

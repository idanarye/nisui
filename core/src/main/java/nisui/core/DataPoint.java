package nisui.core;

/**
 * Represents a data point that Nisui will need to run.
 */
public abstract class DataPoint<D> {
	/**
	 * @return unique identifier of the datapoint
	 */
    public abstract String getKey();
	/**
	 * @return total number of experiments Nisui needs to run on this datapoint
	 */
    public abstract long getNumPlanned();
	/**
	 * @param numPlanned number of experiments Nisui needs to run on this datapoint
	 */
    public abstract void setNumPlanned(long numPlanned);
	/**
	 * @return number of experiments Nisui has already ran on this datapoint
	 */
    public abstract long getNumPerformed();
	/**
	 * @param numPerformed number of experiments Nisui has already ran on this datapoint
	 */
    public abstract void setNumPerformed(long numPerformed);
	/**
	 * @return value of the datapoint to pass to the experiment function
	 */
    public abstract D getValue();
}

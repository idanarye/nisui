package nisui.core;

public interface DataPointInserter<D> extends AutoCloseable {
	public void insert(long numPlanned, long numPerformed, D dataPoint) throws Exception;
}

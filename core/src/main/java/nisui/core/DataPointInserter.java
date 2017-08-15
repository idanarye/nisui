package nisui.core;

public interface DataPointInserter<D> extends AutoCloseable {
	public void insert(D dataPoint) throws Exception;
}

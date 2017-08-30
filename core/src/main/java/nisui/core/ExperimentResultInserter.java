package nisui.core;

public interface ExperimentResultInserter<R> extends AutoCloseable {
	public void insert(DataPoint<?> dataPoint, long seed, R experimentResult) throws Exception;
}

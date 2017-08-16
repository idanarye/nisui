package nisui.core;

public abstract class ResultsStorage<D, R> {
    public abstract void prepareStorage();
    public abstract Connection connect();

    public abstract class Connection implements AutoCloseable {
	public abstract DataPointInserter<D> insertDataPoints();
	public abstract DataPointsReader<D> readDataPoints();
    }
}

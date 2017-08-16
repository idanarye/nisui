package nisui.core;

public interface DataPointsReader<D> extends AutoCloseable, Iterable<DataPoint<D>> {
}

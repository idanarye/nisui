package nisui.core;

public abstract class DataPoint<T> {
	public final T value;

	public DataPoint(T value) {
		this.value = value;
	}
}

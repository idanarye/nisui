package nisui.core;

public abstract class ExperimentResult<T> {
	public final T value;

	public ExperimentResult(T value) {
		this.value = value;
	}
}

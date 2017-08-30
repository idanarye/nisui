package nisui.core;

public interface ExperimentResultsReader<D, R> extends AutoCloseable, Iterable<ExperimentResult<D, R>> {
}

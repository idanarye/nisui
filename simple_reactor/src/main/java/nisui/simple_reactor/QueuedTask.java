package nisui.simple_reactor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.LongSupplier;
import nisui.core.*;

public class QueuedTask<D, R> implements Runnable, Comparable<QueuedTask<D, R>> {
    ExecutionContext<D, R> executionContext;
    private DataPoint<D> dataPoint;
    private LongSupplier seedGenerator;

    public QueuedTask(ExecutionContext<D, R> executionContext, DataPoint<D> dataPoint, LongSupplier seedGenerator) {
        this.executionContext = executionContext;
        this.dataPoint = dataPoint;
        this.seedGenerator = seedGenerator;
    }

    @Override
    public int compareTo(QueuedTask<D, R> other) {
        return Long.compare(this.dataPoint.getNumPerformed(), other.dataPoint.getNumPerformed());
    }

    private synchronized void markExecutedAndReschedule() {
        dataPoint.setNumPerformed(dataPoint.getNumPerformed() + 1);
        if (shouldRun()) {
            executionContext.executor.execute(this);
        }
    }

    @Override
    public void run() {
        markExecutedAndReschedule();
        long seed = seedGenerator.getAsLong();
        try {
            R experimentResult = executionContext.experimentFunctionWrapper.runExperiment(dataPoint.getValue(), seed);
            executionContext.resultsQueue.put(new TaskResult<>(dataPoint, seed, experimentResult));
        } catch (ExperimentFailedException e) {
            executionContext.onException.accept(e);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    public static boolean shouldRun(DataPoint<?> dataPoint) {
        return dataPoint.getNumPerformed() < dataPoint.getNumPlanned();
    }

    public boolean shouldRun() {
        return shouldRun(this.dataPoint);
    }
}

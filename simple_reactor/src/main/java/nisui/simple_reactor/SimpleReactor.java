package nisui.simple_reactor;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;
import nisui.core.*;

/**
 * Runs the experiments.
 */
public class SimpleReactor<D, R> {
    private static Logger logger = LoggerFactory.getLogger(SimpleReactor.class);

    private ResultsStorage<D, R> storage;
    private ExperimentFunctionWrapper<D, R, ?, ?> experimentFunctionWrapper;
    private Consumer<ExperimentFailedException> onException;

    public SimpleReactor(ResultsStorage<D, R> storage, ExperimentFunction<?, ?> experimentFunction, Consumer<ExperimentFailedException> onException) {
        this.storage = storage;
        this.experimentFunctionWrapper = new ExperimentFunctionWrapper<>(storage.getDataPointHandler(), storage.getExperimentResultHandler(), experimentFunction);
        this.onException = onException;
    }

    private boolean shouldRun(DataPoint<?> dataPoint) {
        return dataPoint.getNumPerformed() < dataPoint.getNumPlanned();
    }

    public void run() {
        PriorityBlockingQueue<DataPoint<D>> queue = new PriorityBlockingQueue<>(11, (a, b) -> {
            return Long.compare(a.getNumPerformed(), b.getNumPerformed());
        });
        try (ResultsStorage<D, ?>.Connection con = storage.connect()) {
            try (DataPointsReader<D> reader = con.readDataPoints()) {
                for (DataPoint<D> dataPoint : reader) {
                    if (shouldRun(dataPoint)) {
                        queue.put(dataPoint);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cant connect to database - {}", e);
        }

        Random random = new Random();

        while (!queue.isEmpty()) {
            DataPoint<D> dataPoint = queue.poll();
            long seed = random.nextLong();
            logger.trace("Running {} with seed {}", dataPoint, seed);
            R result;
            try {
                result = experimentFunctionWrapper.runExperiment(dataPoint.getValue(), seed);
            } catch (ExperimentFailedException e) {
                this.onException.accept(e);
                return;
            }
            logger.trace("Got {}", result);
            try (ResultsStorage<D, R>.Connection con = storage.connect()) {
                try (ExperimentResultInserter<R> inserter = con.insertExperimentResults()) {
                    inserter.insert(dataPoint, seed, result);
                }
            } catch (Exception e) {
                logger.error("Cant connect to database - {}", e);
                throw new RuntimeException(e);
            }
            dataPoint.setNumPerformed(dataPoint.getNumPerformed() + 1);
            if (shouldRun(dataPoint)) {
                queue.put(dataPoint);
            }
        }
    }
}

package nisui.simple_reactor;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import nisui.core.*;

/**
 * Runs the experiments.
 */
public class SimpleReactor<D, R> {
    private static Logger logger = LoggerFactory.getLogger(SimpleReactor.class);

    private int numThreads;
    private int maxBunch = 1024;
    private ResultsStorage<D, R> storage;
    private ExperimentFunctionWrapper<D, R, ?, ?> experimentFunctionWrapper;
    private Consumer<ExperimentFailedException> onException;

    public SimpleReactor(int numThreads, ResultsStorage<D, R> storage, ExperimentFunction<?, ?> experimentFunction, Consumer<ExperimentFailedException> onException) {
        this.numThreads = numThreads;
        this.storage = storage;
        this.experimentFunctionWrapper = new ExperimentFunctionWrapper<>(storage.getDataPointHandler(), storage.getExperimentResultHandler(), experimentFunction);
        this.onException = onException;
    }

    public void run() throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(numThreads, numThreads, 10, TimeUnit.SECONDS, new PriorityBlockingQueue<>());
        LinkedBlockingQueue<TaskResult<D, R>> resultsQueue = new LinkedBlockingQueue<>();

        ExecutionContext<D, R> executionContext = new ExecutionContext<>();
        executionContext.experimentFunctionWrapper = this.experimentFunctionWrapper;
        executionContext.resultsQueue = resultsQueue;
        executionContext.onException = this.onException;
        executionContext.executor = executor;

        Random random = new Random();
        try (ResultsStorage<D, ?>.Connection con = storage.connect()) {
            try (DataPointsReader<D> reader = con.readDataPoints()) {
                for (DataPoint<D> dataPoint : reader) {
                    if (QueuedTask.shouldRun(dataPoint)) {
                        executor.execute(new QueuedTask<>(executionContext, dataPoint, random::nextLong));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cant connect to database - {}", e);
        }

        while (true) {
            TaskResult<D, R> result = resultsQueue.poll(50, TimeUnit.MILLISECONDS);
            if (result == null) {
                if (executor.getActiveCount() == 0 && executor.getQueue().isEmpty()) {
                    executor.shutdown();
                    break;
                } else {
                    continue;
                }
            }
            try (ResultsStorage<D, R>.Connection con = storage.connect()) {
                try (ExperimentResultInserter<R> inserter = con.insertExperimentResults()) {
                    inserter.insert(result.dataPoint, result.seed, result.experimentResult);
                    int count = 1;
                    for (int i = 0; i < maxBunch; ++i) {
                        result = resultsQueue.poll();
                        if (result == null) {
                            break;
                        }
                        inserter.insert(result.dataPoint, result.seed, result.experimentResult);
                        ++count;
                    }
                    logger.info("Writing {} results to database", count);
                }
            } catch (Exception e) {
                logger.error("Cant connect to database - {}", e);
                throw new RuntimeException(e);
            }
            // System.out.println(storage.getExperimentResultHandler().formatAsString(result.experimentResult));
        }

        // while (!queue.isEmpty()) {
            // DataPoint<D> dataPoint = queue.poll();
            // long seed = random.nextLong();
            // if (logger.isTraceEnabled()) {
                // logger.trace("Running {} with seed {}", storage.getDataPointHandler().formatAsString(dataPoint.getValue()), seed);
            // }
            // R result;
            // try {
                // result = experimentFunctionWrapper.runExperiment(dataPoint.getValue(), seed);
            // } catch (ExperimentFailedException e) {
                // this.onException.accept(e);
                // return;
            // }
            // if (logger.isTraceEnabled()) {
                // logger.trace("Got {}", storage.getExperimentResultHandler().formatAsString(result));
            // }
            // // try (ResultsStorage<D, R>.Connection con = storage.connect()) {
                // // try (ExperimentResultInserter<R> inserter = con.insertExperimentResults()) {
                    // // inserter.insert(dataPoint, seed, result);
                // // }
            // // } catch (Exception e) {
                // // logger.error("Cant connect to database - {}", e);
                // // throw new RuntimeException(e);
            // // }
            // dataPoint.setNumPerformed(dataPoint.getNumPerformed() + 1);
            // if (shouldRun(dataPoint)) {
                // queue.put(dataPoint);
            // }
        // }
    }
}

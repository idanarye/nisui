package nisui.simple_reactor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

import nisui.core.*;

class ExecutionContext<D, R> {

    ExperimentFunctionWrapper<D, R, ?, ?> experimentFunctionWrapper;
    LinkedBlockingQueue<TaskResult<D, R>> resultsQueue;
    Consumer<ExperimentFailedException> onException;
    ThreadPoolExecutor executor;
}

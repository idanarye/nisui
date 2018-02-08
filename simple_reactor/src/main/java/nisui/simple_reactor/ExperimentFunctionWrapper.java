package nisui.simple_reactor;

import nisui.core.ExperimentFunction;
import nisui.core.ExperimentValuesHandler;
import java.util.function.Function;

public class ExperimentFunctionWrapper<D, R, ED, ER> {
    private Function<D, ED> dpMapper;
    private Function<ER, R> erMapper;
    private ExperimentFunction<ED, ER> experimentFunction;

    public ExperimentFunctionWrapper(ExperimentValuesHandler<D> dpHandler, ExperimentValuesHandler<R> erHandler, ExperimentFunction<ED,ER> experimentFunction) {
        this.dpMapper = dpHandler.createMapper(experimentFunction.getDataPointHandler());
        this.erMapper = experimentFunction.getExperimentResultHandler().createMapper(erHandler);
        this.experimentFunction = experimentFunction;
    }

    public R runExperiment(D dataPoint, long seed) {
        ED dp = dpMapper.apply(dataPoint);
        ER er = experimentFunction.runExperiment(dp, seed);
        return erMapper.apply(er);
    }

}

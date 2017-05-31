package nisui.core;

import java.lang.reflect.Method;
import java.util.Arrays;

import java.util.stream.*;

//public class ExperimentsRunner<F extends ExperimentFunction> {
public class ExperimentsRunner<D extends DataPoint, R extends ExperimentResult> {
	private ExperimentFunction<D, R> experimentFunction;

	public Class<D> dataPointClass;
	public Class<R> experimentResultClass;

	@SuppressWarnings("unchecked")
	public ExperimentsRunner(ExperimentFunction<D, R> experimentFunction) {
		this.experimentFunction = experimentFunction;
		Method method = Arrays.stream(experimentFunction.getClass().getMethods()).filter(m -> {
			if (!"runExperiment".equals(m.getName())) {
				// Not the method we are looking for
				return false;
			}
			if (m.getGenericParameterTypes().length != 2
					|| m.getGenericParameterTypes()[0] != long.class
					|| !(m.getGenericParameterTypes()[1] instanceof Class)
					|| !DataPoint.class.isAssignableFrom(((Class)m.getGenericParameterTypes()[1]))
			   ) {
				// Not the signature we are going to use
				return false;
			   }
			// We only want a subclass of the original
			return m.getGenericReturnType() != ExperimentResult.class
				&& m.getGenericParameterTypes()[1] != DataPoint.class;
		}).findAny().get(); // TODO: make sure there is only one
		this.dataPointClass = (Class)(method.getGenericParameterTypes()[1]);
		this.experimentResultClass = (Class)method.getGenericReturnType();
	}
}

package nisui.java_runner;

import java.io.IOError;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import nisui.core.DataPoint;
import nisui.core.ExperimentFunction;
import nisui.core.ExperimentResult;
import nisui.core.ExperimentValuesHandler;
import nisui.core.ExperimentsRunner;

public abstract class JavaExperimentRunner<DP extends DataPoint, ER extends ExperimentResult>
	implements ExperimentFunction<DP, ER>
{
	public static JavaExperimentRunner load(String mainFile, String... dependencies) {
		try {
			Path path = Paths.get(mainFile);
			String className = path.getFileName().toString();
			className = className.substring(0, className.length() - 5);
			Path tempDir = Files.createTempDirectory("nisui-JavaExperimentRunner-compiled-classes");
			LinkedList<URL> allDependencies = new LinkedList<URL>();

			for (URL url : ((URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs()) {
				allDependencies.add(url);
			}
			for (String dependency : dependencies) {
				allDependencies.add(Paths.get(dependency).toUri().toURL());
			}

			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			String dependenciesStr = allDependencies.stream().map(url -> url.getPath()).collect(Collectors.joining(":"));
			compiler.run(null, null, null, "-cp", dependenciesStr, path.toString(), "-d", tempDir.toString());

			URLClassLoader loader = URLClassLoader.newInstance(Stream.concat(
						allDependencies.stream(),
						Stream.of(tempDir.toUri().toURL())).toArray(l -> new URL[l]));
			return (JavaExperimentRunner)Class.forName(className, true, loader).newInstance();
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	private static boolean isSubClassOf(Class<?> sub, Class<?> of) {
		return sub != of && of.isAssignableFrom(sub);
	}

	private JavaExperimentValuesHandler<DataPoint> dataPointHandler;
	private JavaExperimentValuesHandler<ExperimentResult> experimentResultHandler;

	@SuppressWarnings("unchecked")
	public JavaExperimentRunner() {
		for (Method m : getClass().getMethods()) {
			if (m.getName() != "runExperiment") {
				continue;
			}
			if (!isSubClassOf(m.getReturnType(), ExperimentResult.class)) {
				continue;
			}
			Class<?>[] parameters = m.getParameterTypes();
			if (parameters.length != 2) {
				continue;
			}
			if (!parameters[1].isAssignableFrom(long.class)) {
				continue;
			}
			dataPointHandler = new JavaExperimentValuesHandler((Class<DataPoint>)parameters[0]);
			experimentResultHandler = new JavaExperimentValuesHandler((Class<ER>)m.getReturnType());
			return;
		}
		throw new Error("No appropriate method overriding runExperiment");
	}

	@Override
	public ExperimentValuesHandler<DataPoint> getDataPointHandler() {
		return dataPointHandler;
	}

	@Override
	public ExperimentValuesHandler<ExperimentResult> getExperimentResultHandler() {
		return experimentResultHandler;
	}
}

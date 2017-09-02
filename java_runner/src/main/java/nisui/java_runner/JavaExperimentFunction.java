package nisui.java_runner;

import static javax.tools.JavaCompiler.CompilationTask;

import java.io.IOError;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import nisui.core.ExperimentFunction;
import nisui.core.ExperimentValuesHandler;

public abstract class JavaExperimentFunction<DP, ER>
	implements ExperimentFunction<DP, ER>
{
	public static JavaExperimentFunction load(URI mainFile, URI... dependencies) {
		try {
			Path path = Paths.get(mainFile);
			String className = path.getFileName().toString();
			className = className.substring(0, className.length() - 5);
			Path tempDir = Files.createTempDirectory("nisui-JavaExperimentFunction-compiled-classes");
			LinkedList<URL> allDependencies = new LinkedList<URL>();

			for (URL url : ((URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs()) {
				allDependencies.add(url);
			}
			for (URI dependency : dependencies) {
				allDependencies.add(dependency.toURL());
			}

			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			String dependenciesStr = allDependencies.stream().map(url -> url.getPath()).collect(Collectors.joining(":"));
			// StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
			// LinkedList<String> options = new LinkedList<>();

			// options.add("-classpath");
			// options.add(dependenciesStr);

			// LinkedList<JavaFileObject> compUnits = new LinkedList<>();
			// for (JavaFileObject javaFileObject : fileManager.getJavaFileObjects(path.toString())) {
				// compUnits.add(javaFileObject);
			// }
			// CompilationTask compilationTask = compiler.getTask(null, fileManager, null, options, Arrays.asList("DataPoint", "ExperimentResult"), compUnits);
			// if (!compilationTask.call()) {
				// // TODO: replace with custom exception
				// throw new RuntimeException("Unable to compile");
			// }

			compiler.run(null, null, null, "-cp", dependenciesStr, path.toString(), "-d", tempDir.toString());

			URLClassLoader loader = URLClassLoader.newInstance(Stream.concat(
						allDependencies.stream(),
						Stream.of(tempDir.toUri().toURL())).toArray(l -> new URL[l]));
			Class clazz = Class.forName(className, true, loader);
			return (JavaExperimentFunction)clazz.newInstance();
		} catch (Throwable e) {
			throw new Error(e);
		}
	}

	private static boolean isSubClassOf(Class<?> sub, Class<?> of) {
		return sub != of && of.isAssignableFrom(sub);
	}

	private JavaExperimentValuesHandler<DP> dataPointHandler;
	private JavaExperimentValuesHandler<ER> experimentResultHandler;

	@SuppressWarnings("unchecked")
	public JavaExperimentFunction() {
		for (Method m : getClass().getMethods()) {
			if (m.getName() != "runExperiment") {
				continue;
			}
			Class<?>[] parameters = m.getParameterTypes();
			if (parameters.length != 2) {
				continue;
			}
			if (!parameters[1].isAssignableFrom(long.class)) {
				continue;
			}
			dataPointHandler = new JavaExperimentValuesHandler((Class<DP>)parameters[0]);
			experimentResultHandler = new JavaExperimentValuesHandler((Class<ER>)m.getReturnType());
			return;
		}
		throw new Error("No appropriate method overriding runExperiment");
	}

	@Override
	public ExperimentValuesHandler<DP> getDataPointHandler() {
		return dataPointHandler;
	}

	@Override
	public ExperimentValuesHandler<ER> getExperimentResultHandler() {
		return experimentResultHandler;
	}
}

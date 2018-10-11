package nisui.java_runner;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
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
import nisui.core.ExperimentFunction;
import nisui.core.ExperimentValuesHandler;

public class JavaExperimentFunction<DP, ER> implements ExperimentFunction<DP, ER>
{
    public static <DP, ER> JavaExperimentFunction<DP, ER> load(URI mainFile, URI... dependencies) throws CompilationFailedException {
        try {
            Path path = Paths.get(mainFile);
            String className = path.getFileName().toString();
            className = className.substring(0, className.length() - 5);
            Path tempDir = Files.createTempDirectory("nisui-JavaExperimentFunction-compiled-classes");
            LinkedList<URL> allDependencies = new LinkedList<URL>();

            // for (URL url : ((URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs()) {
                // allDependencies.add(url);
            // }
            for (URI dependency : dependencies) {
                allDependencies.add(dependency.toURL());
            }

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            String dependenciesStr = allDependencies.stream().map(url -> url.getPath()).collect(Collectors.joining(":"));

            ByteArrayOutputStream err = new ByteArrayOutputStream();
            int result = compiler.run(null, null, err, "-cp", dependenciesStr, path.toString(), "-d", tempDir.toString());
            if (0 != result) {
                throw new CompilationFailedException(path.toString(), result, err.toString());
            }

            URLClassLoader loader = URLClassLoader.newInstance(Stream.concat(
                        allDependencies.stream(),
                        Stream.of(tempDir.toUri().toURL())).toArray(l -> new URL[l]));
            Class<?> clazz = Class.forName(className, true, loader);
            return new JavaExperimentFunction<DP, ER>(clazz.getDeclaredConstructor().newInstance());
        } catch (CompilationFailedException e) {
            throw e;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    private static boolean isSubClassOf(Class<?> sub, Class<?> of) {
        return sub != of && of.isAssignableFrom(sub);
    }

    private Object underlyingObject;
    private Method runExperimentMethod;
    private JavaExperimentValuesHandler<DP> dataPointHandler;
    private JavaExperimentValuesHandler<ER> experimentResultHandler;

    @SuppressWarnings("unchecked")
    public JavaExperimentFunction(Object underlyingObject) {
        this.underlyingObject = underlyingObject;
        for (Method m : underlyingObject.getClass().getMethods()) {
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
            this.runExperimentMethod = m;
            this.dataPointHandler = new JavaExperimentValuesHandler<DP>((Class<DP>)parameters[0]);
            this.experimentResultHandler = new JavaExperimentValuesHandler<ER>((Class<ER>)m.getReturnType());
            return;
        }
        throw new Error("No appropriate method overriding runExperiment");
    }

    @Override
    @SuppressWarnings("unchecked")
    public ER runExperiment(DP dataPoint, long seed) {
        try {
            return (ER)runExperimentMethod.invoke(this.underlyingObject, dataPoint, seed);
        } catch (IllegalAccessException e) {
            throw new Error(e);
        } catch (InvocationTargetException e) {
            throw new Error(e);
        }
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

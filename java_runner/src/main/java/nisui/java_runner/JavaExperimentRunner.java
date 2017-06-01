package nisui.java_runner;

import java.io.IOError;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import nisui.core.DataPoint;
import nisui.core.ExperimentFunction;
import nisui.core.ExperimentResult;

public abstract class JavaExperimentRunner {
	public static JavaExperimentRunner load(String mainFile, String... dependencies) {
		try {
			Path path = Paths.get(mainFile);
			System.out.println(path);
			String className = path.getFileName().toString();
			className = className.substring(0, className.length() - 5);
			System.out.println(className);
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			Path tempDir = null;
			tempDir = Files.createTempDirectory("nisui-JavaExperimentRunner-compiled-classes");
			String dependenciesStr = ".";
			URLClassLoader loader = URLClassLoader.newInstance(new URL[] { tempDir.toUri().toURL() });
			loader = URLClassLoader.newInstance(((URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs(), loader);
			for (URL url : ((URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs()) {
				dependenciesStr += ":" + url.getFile();
			}
			if (0 < dependencies.length) {
				dependenciesStr += ":" + String.join(":", dependencies);
			}
			compiler.run(null, null, null, "-cp", dependenciesStr, path.toString(), "-d", tempDir.toString());
			return (JavaExperimentRunner)Class.forName(className, true, loader).newInstance();
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	public abstract void doIt();
}

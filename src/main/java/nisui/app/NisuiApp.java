package nisui.app;

import java.util.Arrays;

import nisui.java_runner.JavaExperimentRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nisui.core.ExperimentFunction;

public class NisuiApp {
	private static Logger logger = LoggerFactory.getLogger(NisuiApp.class);
	public static void main(String[] args) {
		JavaExperimentRunner runner = JavaExperimentRunner.load(args[0], Arrays.copyOfRange(args, 1, args.length));
		if (runner != null) {
			runner.doIt();
		}
	}
}

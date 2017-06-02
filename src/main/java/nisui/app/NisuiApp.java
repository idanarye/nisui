package nisui.app;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nisui.core.*;
import nisui.java_runner.JavaExperimentRunner;

public class NisuiApp {
    private static Logger logger = LoggerFactory.getLogger(NisuiApp.class);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        JavaExperimentRunner runner = JavaExperimentRunner.load(args[0], Arrays.copyOfRange(args, 1, args.length));
        if (runner != null) {
            Scanner scan = new Scanner(System.in);
            LinkedList<DataPoint> dataPoints = new LinkedList<DataPoint>();
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                dataPoints.add(runner.createDataPoint(line));
            }

            while (true) {
                long seed = System.currentTimeMillis();
                for (DataPoint dataPoint : dataPoints) {
                    try {
                        ExperimentResult result = runner.runExperiment(dataPoint, seed);
                        System.out.printf("%s\t%s\t%s\n", dataPoint, seed, result);
                    } catch (Throwable e) {
                        System.out.printf("%s\t%s\t%s\n", dataPoint, seed, e.getClass());
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void run(JavaExperimentRunner runner, String dataPointText, long seed) {
        PrintStream oldErr = System.err;
        PrintStream newErr = new PrintStream(new ByteArrayOutputStream());
        System.setErr(newErr);
        ExperimentResult result = null;
        DataPoint dataPoint = runner.createDataPoint(dataPointText);
        System.out.println(dataPoint);
        try {
            result = runner.runExperiment(dataPoint, seed);
        } finally {
            System.setErr(oldErr);
        }

        System.out.println(result);
    }
}

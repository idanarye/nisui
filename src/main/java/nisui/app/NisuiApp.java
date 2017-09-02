package nisui.app;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nisui.core.*;
import nisui.java_runner.JavaExperimentFunction;
import nisui.java_runner.JavaExperimentValuesHandler;

public class NisuiApp {
    private static Logger logger = LoggerFactory.getLogger(NisuiApp.class);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws URISyntaxException {
        JavaExperimentFunction<Object, ?> runner = JavaExperimentFunction.load(new URI(args[0]), Arrays.stream(args).skip(1).map(arg -> {
            try {
                return new URI(arg);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URI[]::new));
        if (runner != null) {
            Scanner scan = new Scanner(System.in);
            JavaExperimentValuesHandler.Field[] fields = Arrays.stream(scan.nextLine().split("\t"))
                .map(runner.getDataPointHandler()::field)
                .toArray(JavaExperimentValuesHandler.Field[]::new);
            LinkedList<Object> dataPoints = new LinkedList<>();
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                Object dp = runner.getDataPointHandler().createValue();
                String[] parts = line.split("\t");
                assert parts.length == fields.length;
                for (int i = 0; i < parts.length; ++i) {
                    JavaExperimentValuesHandler.Field field = fields[i];
                    Object value = field.parseString(parts[i]);
                    field.set(dp, value);
                }
                dataPoints.add(dp);
            }
            while (true) {
                long seed = System.currentTimeMillis();
                for (Object dataPoint : dataPoints) {
                    try {
                        Object result = runner.runExperiment(dataPoint, seed);
                        System.out.printf("%s\t%s\t%s\n", dataPoint, seed, result);
                    } catch (Throwable e) {
                        System.out.printf("%s\t%s\t%s\n", dataPoint, seed, e.getClass());
                    }
                }
            }
        }
    }
}

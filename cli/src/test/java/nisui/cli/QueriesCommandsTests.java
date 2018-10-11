package nisui.cli;

import org.junit.*;

import java.util.Arrays;
import java.util.HashSet;

import java.util.function.BiFunction;

import org.assertj.core.api.Assertions;

import nisui.java_runner.JavaExperimentFunction;

public class QueriesCommandsTests extends TestsBase {
    public static class Experiment {
        public static class DP {
            public int a;
            public int b;
        }

        public static class R {
            public int sum;
            public int prod;
            public long origSeed;
        }

        public R runExperiment(DP dp, long seed) {
            R r = new R();
            r.sum = dp.a + dp.b;
            r.prod = dp.a * dp.b;
            r.origSeed = seed;
            return r;
        }
    }

    @Test
    public void runExperimentAndQueryResults() {
        EntryPoint entryPoint = getEntryPoint(new Experiment());
        entryPoint.run("data-points", "add", "-n", "10", "a=1", "b=1");
        entryPoint.run("data-points", "add", "-n", "10", "a=1", "b=2");
        entryPoint.run("data-points", "add", "-n", "10", "a=2", "b=1");
        entryPoint.run("data-points", "add", "-n", "10", "a=2", "b=2");
        entryPoint.run("run");
        String output = entryPoint.runGetOutput("--format=csv", "query", "run", "--filter=a + b < 4", "min(sum)", "max(prod)");
        String[] lines = output.split("\n");
        Assertions.assertThat(lines[0]).isEqualTo("a,b,min(sum),max(prod)");
        lines = Arrays.copyOfRange(lines, 1, lines.length);
        HashSet<String> dataPoints = new HashSet<>();

        for (String line : lines) {
            String[] parts = line.split(",");
            int a = Integer.parseInt(parts[0]);
            int b = Integer.parseInt(parts[1]);
            dataPoints.add(String.format("%s%s", a, b));
            double sum = Double.parseDouble(parts[2]);
            double prod = Double.parseDouble(parts[3]);
            Assertions.assertThat(prod).isEqualTo(a * b);
            Assertions.assertThat(sum).isEqualTo(a + b);
        }

        Assertions.assertThat(dataPoints).containsExactlyInAnyOrder("11", "12", "21");
    }
}

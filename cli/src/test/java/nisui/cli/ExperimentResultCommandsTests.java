package nisui.cli;

import org.junit.*;

import java.util.Arrays;

import java.util.function.BiFunction;

import org.assertj.core.api.Assertions;

import nisui.java_runner.JavaExperimentFunction;

public class ExperimentResultCommandsTests extends TestsBase {
    static class Experiment extends JavaExperimentFunction<Experiment.DP, Experiment.R> {
        public static class DP {
            public int a;
            public int b;
        }

        public static class R {
            public int sum;
            public int prod;
            public long origSeed;
        }

        @Override
        public R runExperiment(DP dp, long seed) {
            R r = new R();
            r.sum = dp.a + dp.b;
            r.prod = dp.a * dp.b;
            r.origSeed = seed;
            return r;
        }
    }

    @Test
    public void runExperimentAndListResults() {
        EntryPoint entryPoint = getEntryPoint(new Experiment());
        entryPoint.run("data-points", "add", "-n", "10", "a=1", "b=2");
        entryPoint.run("data-points", "add", "--num-planned", "20", "a=3", "b=4");
        entryPoint.run("run");
        String output = entryPoint.runGetOutput("--format=csv", "experiment-results", "list");
        String[] lines = output.split("\n");
        Assertions.assertThat(lines[0]).isEqualTo("datapoint_key,a,b,seed,sum,prod,origSeed");
        lines = Arrays.copyOfRange(lines, 1, lines.length);
        int[] counters = new int[2];
        for (String line : lines) {
            String[] parts = line.split(",");
            int datapointKey = Integer.parseInt(parts[0]);
            int a = Integer.parseInt(parts[1]);
            int b = Integer.parseInt(parts[2]);
            long seed = Long.parseLong(parts[3]);
            int sum = Integer.parseInt(parts[4]);
            int prod = Integer.parseInt(parts[5]);
            long origSeed = Long.parseLong(parts[6]);

            ++counters[datapointKey - 1];

            switch (datapointKey) {
                case 1:
                    Assertions.assertThat(a).isEqualTo(1);
                    Assertions.assertThat(b).isEqualTo(2);
                    break;
                case 2:
                    Assertions.assertThat(a).isEqualTo(3);
                    Assertions.assertThat(b).isEqualTo(4);
                    break;
                default:
                    Assertions.fail("Unknown datapoint %s", datapointKey);
            }

            Assertions.assertThat(origSeed).isEqualTo(seed);
            Assertions.assertThat(prod).isEqualTo(a * b);
            Assertions.assertThat(sum).isEqualTo(a + b);
        }

        Assertions.assertThat(counters).containsExactly(10, 20);
    }
}

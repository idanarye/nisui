package nisui.cli;

import org.junit.*;
import java.util.function.BiFunction;
import org.assertj.core.api.Assertions;

import nisui.java_runner.JavaExperimentFunction;

public class ExperimentCommandsTests extends TestsBase {
    static class Experiment extends JavaExperimentFunction<Experiment.DP, Experiment.R> {
        public static class DP {
            public int a;
            public int b;
            public int c;
        }

        public static class R {
            public int x;
            public int y;
            public int z;
        }

        @Override
        public R runExperiment(DP dp, long seed) {
            R r = new R();
            r.x = dp.a + dp.b;
            r.y = dp.b + dp.c;
            r.z = dp.c + dp.a;
            return r;
        }
    }

    @Test
    public void experimentInfo() {
        EntryPoint entryPoint = getEntryPoint(new Experiment());
        String output = entryPoint.runGetOutput("experiment", "info");
        Assertions.assertThat(output).isEqualTo(
                "Data Points:\n"
                + "    a: int\n"
                + "    b: int\n"
                + "    c: int\n"
                + "Experiment Results:\n"
                + "    x: int\n"
                + "    y: int\n"
                + "    z: int\n");
    }

    @Test
    public void runExperiment() {
        EntryPoint entryPoint = getEntryPoint(new Experiment());
        String output = entryPoint.runGetOutput("experiment", "run", "a=1", "b=2", "c=3");
        Assertions.assertThat(output).isEqualTo(
                "x: 3\n"
                + "y: 5\n"
                + "z: 4\n");
    }
}

package nisui.cli;

import org.junit.*;
import java.util.function.BiFunction;

import org.assertj.core.api.Assertions;

import nisui.java_runner.JavaExperimentFunction;

public class DataPointCommandsTests extends TestsBase {
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
	public void dataPointsAddAndList() {
		EntryPoint entryPoint = getEntryPoint(new Experiment());
		entryPoint.run("data-points", "add", "-n", "20", "a=1", "b=2", "c=3");
		entryPoint.run("data-points", "add", "--num-planned", "40", "a=4", "b=5", "c=6");
		String output = entryPoint.runGetOutput("--format=csv", "data-points", "list");
		Assertions.assertThat(output).isEqualTo(
				"key,num_planned,num_performed,a,b,c\n"
				+ "1,20,0,1,2,3\n"
				+ "2,40,0,4,5,6\n");
	}
}

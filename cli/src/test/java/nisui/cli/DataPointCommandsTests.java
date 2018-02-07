package nisui.cli;

import org.junit.*;
import java.util.function.BiFunction;
import lombok.*;

import org.assertj.core.api.Assertions;

import nisui.java_runner.JavaExperimentFunction;

public class DataPointCommandsTests extends TestsBase {
	static class Experiment extends JavaExperimentFunction<Experiment.DP, Experiment.R> {
		@Data
		@AllArgsConstructor
		@NoArgsConstructor
		static class DP {
			private int a;
			private int b;
			private int c;
		}

		@Data
		@AllArgsConstructor
		@NoArgsConstructor
		static class R {
			private int x;
			private int y;
			private int z;
		}

		@Override
		public R runExperiment(DP dp, long seed) {
			return new R(dp.a + dp.b, dp.b + dp.c, dp.c + dp.a);
		}
	}

	@Test
	public void dataPointsAddAndList() {
		EntryPoint entryPoint = getEntryPoint(new Experiment());
		entryPoint.run("data-points", "add", "a=1", "b=2", "c=3", "-n", "20");
		entryPoint.run("data-points", "add", "a=4", "b=5", "c=6", "--num-planned", "40");
		String output = entryPoint.runGetOutput("--format=csv", "data-points", "list");
		Assertions.assertThat(output).isEqualTo(
				"key,num_planned,num_performed,a,b,c\n"
				+ "1,20,0,1,2,3\n"
				+ "2,40,0,4,5,6\n");
	}
}

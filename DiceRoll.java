import java.util.Random;

import lombok.*;

import nisui.java_runner.JavaExperimentFunction;

public class DiceRoll extends JavaExperimentFunction<DiceRoll.DP, DiceRoll.R> {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class DP {
        public int min = 1;
        public int faces = 6;
        public int num = 1;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class R {
        public int total;
    }
    @Override
    public R runExperiment(DP dp, long seed) {
        Random random = new Random(seed);
        int total = 0;
        for (int i = 0; i < dp.num; ++i) {
            total += dp.min + random.nextInt(dp.faces);
        }
        return new R(total);
    }
}

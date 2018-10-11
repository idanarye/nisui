import java.util.Random;

public class DiceRoll {
    public static class DP {
        public int min = 1;
        public int faces = 6;
        public int num = 1;
    }

    public static class R {
        public int total;
    }

    public R runExperiment(DP dp, long seed) {
        if (dp.min == 2) {
            throw new RuntimeException("YUP");
        }
        R r = new R();
        Random random = new Random(seed);
        try { Thread.sleep(100); } catch (Exception e) {}
        r.total = 0;
        for (int i = 0; i < dp.num; ++i) {
            r.total += dp.min + random.nextInt(dp.faces);
        }
        return r;
    }
}

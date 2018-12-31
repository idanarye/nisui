import java.util.Random;

public class DiceRoller {
    public static class DataPoint {
        public int minValue;
        public int maxValue;
        public int numDice;
    }

    public static class ExperimentResult {
        public int totalResult;
    }

    public ExperimentResult runExperiment(DataPoint dataPoint, long seed) {
        Random random = new Random(seed);
        final int rangeSize = dataPoint.maxValue - dataPoint.minValue;
        int total = 0;
        for (int i = 0; i < dataPoint.numDice; ++ i) {
            total += dataPoint.minValue + random.nextInt(rangeSize);
        }
        ExperimentResult result = new ExperimentResult();
        result.totalResult = total;
        return result;
    }
}

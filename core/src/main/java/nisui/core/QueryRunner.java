package nisui.core;

public interface QueryRunner<D> extends AutoCloseable, Iterable<QueryRunner.Row<D>> {
    public static class Row<D> {
        public final D dataPoint;
        public double[] values;

        public Row(D dataPoint, double[] values) {
            this.dataPoint = dataPoint;
            this.values = values;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(dataPoint);
            builder.append('(');
            for (int i = 0; i < values.length; ++i) {
                if (0 < i) {
                    builder.append(", ");
                }
                builder.append(values[i]);
            }
            builder.append(')');
            return builder.toString();
        }
    }
}

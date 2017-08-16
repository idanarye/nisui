package nisui.h2_store;

import nisui.core.DataPoint;

public class H2DataPoint<T> extends DataPoint<T> {
    private long id;

    public H2DataPoint(T value, long id) {
        super(value);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", id, value);
    }
}

package nisui.h2_store;

import nisui.core.DataPoint;

public class H2DataPoint<D> extends DataPoint<D> {
    private long id;
    private D value;

    public H2DataPoint(long id, D value) {
        this.id = id;
        this.value = value;
    }

    public long getId() {
        return id;
    }

	public D getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", id, value);
    }
}

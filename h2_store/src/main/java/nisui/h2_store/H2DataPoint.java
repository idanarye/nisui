package nisui.h2_store;

import nisui.core.DataPoint;

public class H2DataPoint<D> extends DataPoint<D> {
    private long id;
    private long numPlanned;
    private long numPerformed;
    private D value;

    public H2DataPoint(long id, long numPlanned, long numPerformed, D value) {
        this.id = id;
        this.numPlanned = numPlanned;
        this.numPerformed = numPerformed;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public String getKey() {
        return String.valueOf(id);
    }

    public long getNumPlanned() {
        return numPlanned;
    }
    public void setNumPlanned(long numPlanned) {
        this.numPlanned = numPlanned;
    }
    public long getNumPerformed() {
        return numPerformed;
    }
    public void setNumPerformed(long numPerformed) {
        this.numPerformed = numPerformed;
    }

    public D getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", id, value);
    }
}

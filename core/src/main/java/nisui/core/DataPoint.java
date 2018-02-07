package nisui.core;

public abstract class DataPoint<D> {
    public abstract String getKey();
    public abstract long getNumPlanned();
    public abstract void setNumPlanned(long num);
    public abstract long getNumPerformed();
    public abstract void setNumPerformed(long num);
    public abstract D getValue();
}

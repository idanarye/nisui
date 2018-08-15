package nisui.core;

import nisui.core.plotting.PlotEntry;

public interface StoredPlotSaver extends AutoCloseable {
	public void save(PlotEntry plotEntry) throws Exception;
}

package nisui.gui.queries

import nisui.core.plotting.*

class PlotListEntry(var plot: PlotEntry, var status: PlotListEntry.Status) {
    enum class Status { SYNCED, UNSYNCED, TO_DELETE }

    fun getName() = plot.getName()
    fun setName(name: String) = plot.setName(name)
}

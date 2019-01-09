package nisui.gui.queries

import javax.swing.*
import javax.swing.event.*
import javax.swing.table.*

import nisui.core.plotting.*

import nisui.gui.*

class PlotsList(val parent: PlotSettingPanel): TablePanel<PlotListEntry>() {
    fun createResultsStorage() = parent.createResultsStorage()

    val plots: MutableList<PlotListEntry>

    init {
        plots = createResultsStorage().connect().readStoredPlots().use {
            it.map({PlotListEntry(it, PlotListEntry.Status.SYNCED)}).toMutableList()
        }
        table.getModel().addTableModelListener {
        }
        table.getSelectionModel().addListSelectionListener {
            val row = table.getSelectedRow()
            if (row < 0) {
            } else if (row == plots.size) {
            } else {
                parent.changeFocusedPlot(plots[row])
            }
        }
    }

    override protected fun getRowsSource(): List<PlotListEntry> {
        return plots
    }

    override protected fun addNewEntry(): PlotListEntry {
        val entry = PlotListEntry(PlotEntry.buildNew(""), PlotListEntry.Status.UNSYNCED)
        plots.add(entry)
        return entry
    }

    override protected fun deleteEntry(index: Int) {
        /* parent.entry.getAxes().removeAt(index) */
    }

    override protected fun populateColumns() {
        columns.add(Column("Name", PlotListEntry::getName, PlotListEntry::setName))
        columns.add(Column("Status", PlotListEntry::status))
    }
}

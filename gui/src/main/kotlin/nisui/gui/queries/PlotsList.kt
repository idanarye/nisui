package nisui.gui.queries

import javax.swing.*
import javax.swing.event.*
import javax.swing.table.*

import nisui.core.plotting.*

import nisui.gui.*

class PlotsList(val parent: PlotSettingPanel): TablePanel<PlotListEntry>() {

    val plots = mutableListOf<PlotListEntry>()

    init {
        with (addNewEntry().plot) {
            setName("One")
            getAxes().add(PlotAxis("X", ScaleType.LINEAR, "things", "1 + 2"))
        }
        with (addNewEntry().plot) {
            setName("Two")
            getAxes().add(PlotAxis("Y", ScaleType.LOGARITHMIC, "stuff", "3 * 4"))
        }
        with (addNewEntry().plot) {
            setName("Three")
            getAxes().add(PlotAxis("Z", ScaleType.LOGARITHMIC, "stuff", "3 * 4"))
        }
        plots.forEach {it.status = PlotListEntry.Status.SYNCED}
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

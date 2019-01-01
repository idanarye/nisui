package nisui.gui.queries

import javax.swing.*
import javax.swing.event.*
import javax.swing.table.*

import nisui.core.plotting.*

import nisui.gui.*

class PlotsList(val parent: PlotSettingPanel): TablePanel<PlotsList.ListEntry>() {
    enum class EntryStatus { SYNCED, UNSYNCED, TO_DELETE }
    class ListEntry(var plot: PlotEntry, var status: EntryStatus) {
        fun getName() = plot.getName()
        fun setName(name: String) = plot.setName(name)
    }

    val plots = mutableListOf<ListEntry>()

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
        table.getModel().addTableModelListener {
        }
        table.getSelectionModel().addListSelectionListener {
            val row = table.getSelectedRow()
            if (row == plots.size) {
            } else {
                parent.changeFocusedPlot(plots[row].plot)
            }
        }
    }

    override protected fun getRowsSource(): List<ListEntry> {
        return plots
    }

    override protected fun addNewEntry(): ListEntry {
        val entry = ListEntry(PlotEntry.buildNew(""), EntryStatus.UNSYNCED)
        plots.add(entry)
        return entry
    }

    override protected fun deleteEntry(index: Int) {
        /* parent.entry.getAxes().removeAt(index) */
    }

    override protected fun populateColumns() {
        columns.add(Column("Name", ListEntry::getName, ListEntry::setName))
    }
}

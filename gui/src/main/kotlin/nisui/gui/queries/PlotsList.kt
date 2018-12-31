package nisui.gui.queries

import javax.swing.*
import javax.swing.event.*
import javax.swing.table.*

import nisui.core.plotting.*

import nisui.gui.*

class PlotsList(val parent: PlotSettingPanel): TablePanel<PlotEntry>() {
    init {
        table.getModel().addTableModelListener {
        }
    }

    override protected fun getRowsSource(): List<PlotEntry> {
        return listOf()
    }

    override protected fun addNewEntry(): PlotEntry {
        val entry = PlotEntry.buildNew("")
        /* parent.entry.getAxes().add(entry) */
        return entry
    }

    override protected fun deleteEntry(index: Int) {
        /* parent.entry.getAxes().removeAt(index) */
    }

    override protected fun populateColumns() {
        columns.add(Column("Name", PlotEntry::getName, PlotEntry::setName))
        /* columns.add(Column("Scale Type", PlotEntry::getScaleType, PlotEntry::setScaleType)) */
        /* columns.add(Column("Unit Name", PlotEntry::getUnitName, PlotEntry::setUnitName)) */
        /* columns.add(Column("Expression", PlotEntry::getExpression, PlotEntry::setExpression)) */
    }
}

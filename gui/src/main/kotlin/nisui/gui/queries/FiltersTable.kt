package nisui.gui.queries

import javax.swing.*
import javax.swing.table.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.FlowLayout

import nisui.core.plotting.*

import nisui.gui.*

class FiltersTable(val parent: PlotSettingPanel): TablePanel<PlotFilter>() {
    override fun makeBorder() = BorderFactory.createTitledBorder("Filters")

    init {
        table.getModel().addTableModelListener {
            parent.plotUpdated()
        }
    }

    override protected fun getRowsSource(): List<PlotFilter> {
        return parent.focusedPlot.getFilters()
    }

    override protected fun addNewEntry(): PlotFilter {
        val entry = PlotFilter("", FilterType.NUMERIC_SINGLE, "", "")
        parent.focusedPlot.getFilters().add(entry)
        return entry
    }

    override protected fun deleteEntry(index: Int) {
        parent.focusedPlot.getFilters().removeAt(index)
    }

    override protected fun populateColumns() {
        columns.add(Column("Caption", PlotFilter::getCaption, PlotFilter::setCaption))
        columns.add(Column("Filter Type", PlotFilter::getFilterType, PlotFilter::setFilterType))
        columns.add(Column("Unit Name", PlotFilter::getUnitName, PlotFilter::setUnitName))
        columns.add(Column("Expression", PlotFilter::getExpression, PlotFilter::setExpression))
    }
}

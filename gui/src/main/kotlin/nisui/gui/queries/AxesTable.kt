package nisui.gui.queries

import javax.swing.*
import javax.swing.table.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.FlowLayout

import nisui.core.plotting.*

import nisui.gui.*

class AxesTable(val parent: PlotSettingPanel): TablePanel<PlotAxis>() {
    init {
        table.getModel().addTableModelListener {
            parent.plotUpdated()
        }
    }

    override protected fun getRowsSource(): List<PlotAxis> {
        return parent.focusedPlot.getAxes()
    }

    override protected fun addNewEntry(): PlotAxis {
        val entry = PlotAxis("", ScaleType.LINEAR, "", "")
        parent.focusedPlot.getAxes().add(entry)
        return entry
    }

    override protected fun deleteEntry(index: Int) {
        parent.focusedPlot.getAxes().removeAt(index)
    }

    override protected fun populateColumns() {
        columns.add(Column("Caption", PlotAxis::getCaption, PlotAxis::setCaption))
        columns.add(Column("Scale Type", PlotAxis::getScaleType, PlotAxis::setScaleType))
        columns.add(Column("Unit Name", PlotAxis::getUnitName, PlotAxis::setUnitName))
        columns.add(Column("Expression", PlotAxis::getExpression, PlotAxis::setExpression))
    }
}

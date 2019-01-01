package nisui.gui.queries

import javax.swing.*
import javax.swing.table.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.FlowLayout

import nisui.core.plotting.*

import nisui.gui.*

class FormulasTable(val parent: PlotSettingPanel): TablePanel<PlotFormula>() {
    init {
        table.getModel().addTableModelListener {
            parent.plotUpdated()
        }
    }

    override protected fun getRowsSource(): List<PlotFormula> {
        return parent.focusedPlot.getFormulas()
    }

    override protected fun addNewEntry(): PlotFormula {
        val entry = PlotFormula("", "", ScaleType.LINEAR, "", "")
        parent.focusedPlot.getFormulas().add(entry)
        return entry
    }

    override protected fun deleteEntry(index: Int) {
        parent.focusedPlot.getFormulas().removeAt(index)
    }

    override protected fun populateColumns() {
        columns.add(Column("Caption", PlotFormula::getCaption, PlotFormula::setCaption))
        columns.add(Column("Symbol", PlotFormula::getSymbol, PlotFormula::setSymbol))
        columns.add(Column("Scale Type", PlotFormula::getScaleType, PlotFormula::setScaleType))
        columns.add(Column("Unit Name", PlotFormula::getUnitName, PlotFormula::setUnitName))
        columns.add(Column("Expression", PlotFormula::getExpression, PlotFormula::setExpression))
    }
}

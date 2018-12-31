package nisui.gui.queries

import javax.swing.*
import javax.swing.table.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.FlowLayout

import nisui.core.plotting.*

import nisui.gui.*

class AxesPanel(val parent: PlotSettingPanel): TablePanel<PlotAxis>() {
    init {
        parent.entry.getAxes().add(PlotAxis("X", ScaleType.LINEAR, "things", "1 + 2"))
        parent.entry.getAxes().add(PlotAxis("Y", ScaleType.LOGARITHMIC, "stuff", "3 * 4"))
        /* setModel(AxisModel()) */
        /* with(getColumnModel()) { */
            /* getColumn(0).setHeaderValue("hi") */
            /* getColumn(1).setHeaderValue("hi") */
            /* getColumn(2).setHeaderValue("hi") */
            /* getColumn(3).setHeaderValue("hi") */
        /* } */
        table.getModel().addTableModelListener {
            println("Need to update: ${parent.entry}")
        }
    }

    override protected fun getRowsSource(): List<PlotAxis> {
        return parent.entry.getAxes()
    }

    override protected fun populateColumns() {
        columns.add(Column("Caption", PlotAxis::getCaption, PlotAxis::setCaption))
        columns.add(Column("Scale Type", PlotAxis::getScaleType, PlotAxis::setScaleType))
        columns.add(Column("Unit Name", PlotAxis::getUnitName, PlotAxis::setUnitName))
        columns.add(Column("Expression", PlotAxis::getExpression, PlotAxis::setExpression))
    }

/* class AxesPanel(val parent: PlotSettingPanel): TablePanel<AxisRow>(AxisRow::class) { */
    /* init { */
        /* add(JLabel("AXES"), constraints { */
            /* gridx = 0 */
            /* gridy = 0 */
        /* }) */
        /* add(JLabel("Caption"), constraints { */
            /* gridx = 0 */
            /* gridy = 1 */
        /* }) */
        /* add(JLabel("ScaleType"), constraints { */
            /* gridx = 1 */
            /* gridy = 1 */
        /* }) */
        /* add(JLabel("UnitName"), constraints { */
            /* gridx = 2 */
            /* gridy = 1 */
        /* }) */
        /* add(JLabel("Expression"), constraints { */
            /* gridx = 3 */
            /* gridy = 1 */
        /* }) */

        /* add(JButton("+"), constraints { */
            /* gridx = 0 */
            /* gridy = 2 */
        /* }) */
        /* add(JButton("-"), constraints { */
            /* gridx = 0 */
            /* gridy = 2 */
        /* }) */
    /* } */

    /* override fun createNewRow(): AxisRow { */
        /* return AxisRow() */
    /* } */
}

/* class AxisModel: AbstractTableModel() { */
    /* override fun getColumnCount(): Int = 4 */
    /* override fun getRowCount(): Int = 1 */
    /* override fun getValueAt(row: Int, col: Int) = 12 */
/* } */

/* class AxisRow { */
    /* @Column("Caption") val caption = JTextField() */
    /* @Column("Scale Type") val scaleType = JTextField() */
    /* @Column("Unit Name") val unitName = JTextField() */
    /* @Column("Expression") val expression = JTextField() */
/* } */

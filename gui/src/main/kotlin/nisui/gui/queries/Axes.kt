package nisui.gui.queries

import javax.swing.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.FlowLayout

import nisui.gui.*

/* class AxesPanel(val parent: PlotSettingPanel): JPanel(GridBagLayout()) { */
class AxesPanel(val parent: PlotSettingPanel): TablePanel<AxisRow>(AxisRow::class) {
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

    override fun createNewRow(): AxisRow {
        return AxisRow()
    }
}

class AxisRow {
    @Column("Caption") val caption = JTextField()
    @Column("Scale Type") val scaleType = JTextField()
    @Column("Unit Name") val unitName = JTextField()
    @Column("Expression") val expression = JTextField()
}

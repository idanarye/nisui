package nisui.gui.queries

import javax.swing.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.FlowLayout

import nisui.core.plotting.*

import nisui.gui.*

public class PlotSettingPanel(val parent: PlotsPanel): JPanel(GridBagLayout()) {
    var entry = PlotEntry.buildNew("")!!
    val axesPanel = AxesPanel(this)

    init {
        /* add(verticalJPanel { */
            /* add(horizontalJPanel { */
                /* add(JLabel("Name:")) */
                /* add(JTextField(20)) */
            /* }) */
        /* }) */
        add(gridBagJPanel {
            add(JLabel("Name:"))
            add(JTextField())
        })
        add(axesPanel)
        /* add(JTextField("one"), constraints { */
            /* gridx = 0 */
            /* gridy = 0 */
            /* gridwidth = 2 */
        /* }) */
        /* add(JTextField("two"), constraints { */
            /* gridx = 0 */
            /* gridy = 1 */
        /* }) */
        /* add(JTextField("three"), constraints { */
            /* gridx = 1 */
            /* gridy = 1 */
        /* }) */
    }
}

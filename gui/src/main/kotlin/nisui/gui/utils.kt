package nisui.gui

import javax.swing.*
import java.awt.Color
import java.awt.Container
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.GridLayout
import java.awt.LayoutManager

fun constraints(block: GridBagConstraints.() -> Unit): GridBagConstraints {
    val result = GridBagConstraints()
    result.block()
    return result
}

/* fun jpanel_with_layout(layout: LayoutManager, block: JPanel.() -> Unit): JPanel { */
    /* val panel = JPanel(layout) */
    /* panel.block() */
    /* return panel */
/* } */
fun horizontalJPanel(block: JPanel.() -> Unit): JPanel {
    val panel = JPanel()
    panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))
    panel.block()
    return panel
}

fun verticalJPanel(block: JPanel.() -> Unit): JPanel {
    val panel = JPanel()
    panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
    panel.block()
    return panel
}

fun gridBagJPanel(block: JPanel.() -> Unit): JPanel {
    val panel = JPanel(GridLayout())
    panel.block()
    return panel
}

fun _dbg_showChildrenBorders(container: Container) {
    for (component in container.getComponents()) {
        if (component is JComponent) {
            component.setBorder(BorderFactory.createLineBorder(Color.cyan));
        }
    }
}

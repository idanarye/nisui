package nisui.gui.queries

import javax.swing.JPanel
import java.awt.GridBagLayout
import java.awt.GridBagConstraints

import nisui.core.NisuiFactory;

import nisui.gui.MainFrame
import nisui.gui.*

public class PlotsPanel(val nisuiFactory: NisuiFactory): JPanel(GridBagLayout()) {
    val settingPanel = PlotSettingPanel(this)
    val viewPanel = PlotViewPanel(this)

    init {
        add(settingPanel, constraints {
            gridx = 0
            gridy = 0
            fill = GridBagConstraints.BOTH
        })
        add(viewPanel, constraints {
            gridx = 0
            gridy = 1
            fill = GridBagConstraints.BOTH
            weightx = 1.0
            weighty = 1.0
        })
        // _dbg_showChildrenBorders(this)
        // viewPanel.renderPlot(settingPanel.plotsListPanel.plots[0].plot)
    }

    fun createResultsStorage() = nisuiFactory.createResultsStorage()
}

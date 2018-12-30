package nisui.gui.queries

import javax.swing.JPanel

import nisui.core.NisuiFactory;

import nisui.gui.MainFrame

public class PlotsPanel(val nisuiFactory: NisuiFactory): JPanel() {
    init {
        add(PlotSettingPanel(this))
    }
}

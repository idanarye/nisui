package nisui.gui

import java.io.InputStream
import java.io.PrintStream

import nisui.core.NisuiAppLauncher
import nisui.core.NisuiFactory

public class NisuiGuiLauncher: NisuiAppLauncher {
    override fun run(nisuiFactory: NisuiFactory, sin: InputStream, sout: PrintStream) {
        val mainFrame = MainFrame(nisuiFactory, sin, sout);
        mainFrame.setVisible(true);
    }
}

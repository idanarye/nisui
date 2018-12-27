package nisui.gui;

import java.io.InputStream;
import java.io.PrintStream;

import nisui.core.NisuiAppLauncher;
import nisui.core.NisuiFactory;

public class NisuiGuiLauncher implements NisuiAppLauncher {
    @Override
    public void run(NisuiFactory nisuiFactory, InputStream in, PrintStream out) {
        MainFrame mainFrame = new MainFrame(nisuiFactory, in, out);
        mainFrame.setVisible(true);
    }
}

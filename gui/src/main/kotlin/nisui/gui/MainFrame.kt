package nisui.gui;

import java.io.InputStream;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import nisui.core.NisuiFactory;

import nisui.gui.queries.PlotsPanel;

public class MainFrame(val nisuiFactory: NisuiFactory, val sin: InputStream, val sout: PrintStream): JFrame("Nisui") {
    val menuBar = JMenuBar();
    val tabbedPane = JTabbedPane();

    init {
        nisuiFactory.createResultsStorage().prepareStorage()
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setJMenuBar(menuBar);

        addMenuItem("Plot", KeyEvent.VK_P) {
            tabbedPane.addTab("Plot", PlotsPanel(nisuiFactory));
        }

        setContentPane(tabbedPane);
    }

    fun addMenuItem(caption: String, altShortcutKey: Int? = 0, action: () -> Unit): JMenuItem {
        val menuItem = JMenuItem(caption);
        menuItem.addActionListener {action()};
        if (altShortcutKey != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(altShortcutKey, ActionEvent.ALT_MASK));
            menuItem.setMnemonic(altShortcutKey);
        }
        menuBar.add(menuItem);
        return menuItem;
    }
}

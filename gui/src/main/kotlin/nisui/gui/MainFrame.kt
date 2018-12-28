package nisui.gui;

import java.io.InputStream;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;

import nisui.core.NisuiFactory;

public class MainFrame(val nisuiFactory: NisuiFactory, val sin: InputStream, val sout: PrintStream): JFrame("Nisui") {
    val menuBar = JMenuBar();
    val tabbedPane = JTabbedPane();

    init {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setJMenuBar(menuBar);

        addMenuItem("Query") {
            tabbedPane.addTab("Queries", QueriesPanel(this));
        }

        setContentPane(tabbedPane);
    }

    fun addMenuItem(caption: String, action: () -> Unit) {
        val menuItem = JMenuItem(caption);
        menuItem.addActionListener {action()}
        menuBar.add(menuItem);
    }
}

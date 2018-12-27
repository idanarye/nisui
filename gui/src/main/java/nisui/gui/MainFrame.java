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

public class MainFrame extends JFrame {
	final NisuiFactory nisuiFactory;
	final InputStream in;
	final PrintStream out;

	private JTabbedPane tabbedPane;

	private JMenuBar menuBar;

	public MainFrame(NisuiFactory nisuiFactory, InputStream in, PrintStream out) {
		super("Nisui");
		this.nisuiFactory = nisuiFactory;
		this.in = in;
		this.out = out;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		addMenuItem("Query", () -> {
			tabbedPane.addTab("Queries", new QueriesPanel(this));
		});

		tabbedPane = new JTabbedPane();
		setContentPane(tabbedPane);
	}

	void addMenuItem(String caption, Runnable action) {
		JMenuItem menuItem = new JMenuItem(caption);
		menuItem.addActionListener(e -> action.run());
		menuBar.add(menuItem);
	}
}

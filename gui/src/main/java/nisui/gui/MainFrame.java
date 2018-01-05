package nisui.gui;

import javax.swing.JFrame;

public class MainFrame extends JFrame {
	private HomePanel homePanel;
	public MainFrame() {
		super("Nisui");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		homePanel = new HomePanel(this);
		homePanel.setName("home-panel");
		setContentPane(homePanel);
	}
}

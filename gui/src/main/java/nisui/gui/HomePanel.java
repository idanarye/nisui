package nisui.gui;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class HomePanel extends JPanel {
	private MainFrame parent;

	JTextField databaseField;

	public HomePanel(MainFrame parent) {
		super();
		this.parent = parent;

		databaseField = new JTextField();
		databaseField.setName("database");
		databaseField.setColumns(20);
		add(databaseField);
	}
}

package nisui.gui;

import java.net.URL;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.Test;

public class ExperimentRunningControlTest extends TestsBase {
	@Test
	public void createTablesFromExperimentRunner() {
		MainFrame mainFrame = GuiActionRunner.execute(MainFrame::new);
		FrameFixture frame = new FrameFixture(mainFrame);
		frame.show();

		URL resource = getClass().getClassLoader().getResource("DiceRoller.java");
		frame.panel("home-panel").textBox("database").enterText(resource.getPath());
	}
}

package nisui.cli;

import java.io.InputStream;
import java.io.PrintStream;

interface SubCommand {
	String[] getNames();
	void run(InputStream in, PrintStream out);
}

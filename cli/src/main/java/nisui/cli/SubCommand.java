package nisui.cli;

import java.io.InputStream;
import java.io.PrintStream;

interface SubCommand {
	void run(InputStream in, PrintStream out);
}

package nisui.core;

import java.io.InputStream;
import java.io.PrintStream;

public interface NisuiAppLauncher {
	public void run(NisuiFactory nisuiFactory, InputStream in, PrintStream out);
}

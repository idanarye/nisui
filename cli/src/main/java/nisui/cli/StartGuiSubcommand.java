package nisui.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import nisui.core.NisuiAppLauncher;
import nisui.core.NisuiFactory;
import picocli.CommandLine;

@CommandLine.Command(
name = "gui",
description = "Start the GUI")
public class StartGuiSubcommand extends SubCommand {
    private NisuiFactory nisuiFactory;

    public StartGuiSubcommand(NisuiFactory nisuiFactory) {
        this.nisuiFactory = nisuiFactory;
    }

    @Override
    public String[] getNames() {
        return new String[]{"gui"};
    }

    @Override
    public void run(InputStream in, PrintStream out) {
        try {
            NisuiAppLauncher appLanucher = NisuiAppLauncher.class.cast(Class.forName("nisui.gui.NisuiGuiLauncher").getDeclaredConstructor().newInstance());
            appLanucher.run(nisuiFactory, in, out);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ExitException(e.getMessage());
        }
    }
}

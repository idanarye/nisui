package nisui.cli

import java.io.InputStream
import java.io.PrintStream

import picocli.CommandLine

import nisui.core.NisuiAppLauncher;
import nisui.core.NisuiFactory

@CommandLine.Command(
    name = "gui",
    description = ["Start the GUI"])
public class StartGuiSubcommand(nisuiFactory: NisuiFactory) : CommandGroup(nisuiFactory) {
    override fun getNames(): Array<String> {
        return arrayOf("gui")
    }

    override fun run(in_: InputStream?, out_: PrintStream) {
        val appLanucher = Class.forName("nisui.gui.NisuiGuiLauncher").getDeclaredConstructor().newInstance() as NisuiAppLauncher
        appLanucher.run(nisuiFactory, in_, out_)
    }
}

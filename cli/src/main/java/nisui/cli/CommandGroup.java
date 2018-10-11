package nisui.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import nisui.cli.print_formats.PrintFormat;
import nisui.core.ExperimentValuesHandler;
import nisui.core.NisuiFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public abstract class CommandGroup extends SubCommand {
    private static Logger logger = LoggerFactory.getLogger(CommandGroup.class);

    protected NisuiFactory nisuiFactory;

    Supplier<PrintFormat<?>> printFormatSupplier;
    @SuppressWarnings("unchecked")
    <T> PrintFormat<T> createPrintFormat() {
        return (PrintFormat) printFormatSupplier.get();
    }

    @Override
    public void run(InputStream in, PrintStream out) {
        return;
    }

    protected CommandGroup(NisuiFactory nisuiFactory) {
        this.nisuiFactory = nisuiFactory;
    }

    CommandLine register(CommandLine commandLine) {
        CommandLine commandGroup = super.register(commandLine);

        for (Class<?> clazz : this.getClass().getDeclaredClasses()) {
            if (SubCommand.class.isAssignableFrom(clazz)) {
                Class<? extends SubCommand> subClass = clazz.asSubclass(SubCommand.class); this.registerSubcommand(commandGroup, subClass);
            }
        }

        return commandGroup;
    }

    private <T extends SubCommand> void registerSubcommand(CommandLine commandGroup, Class<T> clazz) {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor(this.getClass());
            T subCommand = ctor.newInstance(new Object[]{this});
            for (String name : subCommand.getNames()) {
                commandGroup.addSubcommand(name, subCommand);
            }
        } catch ( NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.warn("Can't instantiate %s - %s", clazz, e);
        }
    }

    public static <V> V parseValueAssignment(ExperimentValuesHandler<V> valuesHandler, Iterable<String> valueAssignments) {
        V value = valuesHandler.createValue();
        if (valueAssignments != null) for (String assignment : valueAssignments) {
            String[] parts = assignment.split("=", 2);
            ExperimentValuesHandler<V>.Field field = valuesHandler.field(parts[0]);
            field.set(value, field.parseString(parts[1]));
        }
        return value;
    }
}

package nisui.cli;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import nisui.cli.print_formats.PrintFormat;
import nisui.core.ExperimentValuesHandler;
import nisui.core.NisuiFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public abstract class CommandGroup {
    private static Logger logger = LoggerFactory.getLogger(CommandGroup.class);

    protected NisuiFactory nisuiFactory;
    private String[] names;

    Supplier<PrintFormat<?>> printFormatSupplier;
    @SuppressWarnings("unchecked")
    <T> PrintFormat<T> createPrintFormat() {
        return (PrintFormat) printFormatSupplier.get();
    }

    protected CommandGroup(NisuiFactory nisuiFactory, String... names) {
        this.nisuiFactory = nisuiFactory;
        this.names = names;
    }

    void register(CommandLine commandLine) {
        CommandLine commandGroup = new CommandLine(this);
        for (String name : this.names) {
            commandLine.addSubcommand(name, commandGroup);
        }

        for (Class<?> clazz : this.getClass().getDeclaredClasses()) {
            if (SubCommand.class.isAssignableFrom(clazz)) {
                Class<? extends SubCommand> subClass = clazz.asSubclass(SubCommand.class); this.registerSubcommand(commandGroup, subClass);
            }
        }
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

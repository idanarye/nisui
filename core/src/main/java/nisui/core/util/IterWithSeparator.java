package nisui.core.util;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class IterWithSeparator {
    private IterWithSeparator() {
    }

    public static <T> void iterWithSep(Iterable<T> iterable, Consumer<T> onMember, Runnable onSeparator) {
        Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            onMember.accept(iterator.next());
        } else {
            return;
        }

        while (iterator.hasNext()) {
            onSeparator.run();
            onMember.accept(iterator.next());
        }
    }

    public static <T> void iterWithSep(Stream<T> stream, Consumer<T> onMember, Runnable onSeparator) {
        iterWithSep(stream::iterator, onMember, onSeparator);
    }
}

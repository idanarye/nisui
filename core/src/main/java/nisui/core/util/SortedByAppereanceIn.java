package nisui.core.util;

import java.util.HashMap;
import java.util.stream.Stream;

public final class SortedByAppereanceIn {
    private SortedByAppereanceIn() {
    }

    public static <T> Stream<T> sortedByIndexIn(Stream<T> stream, Iterable<T> indexSource, boolean reverse, boolean missingFirst) {
        HashMap<T, Integer> arrayIndex = new HashMap<>();
        int i = 0;
        int sig = reverse ? -1 : 1;
        for (T item : indexSource) {
            arrayIndex.put(item, i * sig);
            ++i;
        }
        int indexForMissing = missingFirst ? -i : i;
        return stream.sorted((a, b) -> Integer.compare(
                   arrayIndex.getOrDefault(a, indexForMissing),
                   arrayIndex.getOrDefault(b, indexForMissing)));
    }

    public static <T> Stream<T> sortedByIndexIn(Stream<T> stream, Stream<T> indexSource, boolean reverse, boolean missingFirst) {
        return sortedByIndexIn(stream, indexSource::iterator, reverse, missingFirst);
    }
}

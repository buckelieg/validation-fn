package buckelieg.validation;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

final class Utils {

    private Utils() {
        throw new UnsupportedOperationException("No instances of Utils");
    }

    /**
     * Taken from here: https://stackoverflow.com/a/47181151
     */
    static final Pattern PATTERN_EMAIL = Pattern.compile("^[\\w-+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-zA-Z]{2,})$");

    /**
     * Taken from here: https://stackoverflow.com/a/31138716
     */
    static final Pattern PATTERN_IPv4_ADDRESS = Pattern.compile("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))");

    static <E> Stream<E> toStream(Iterable<E> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    static <T extends Comparable<T>> Predicate<T> isMeasuredAt(ToIntFunction<T> mapper, Predicate<Integer> predicate) {
        requireNonNull(mapper, "ToIntFunction must be provided");
        requireNonNull(predicate, "Predicate must be provided");
        return value -> predicate.test(mapper.applyAsInt(value));
    }

    static boolean allCharactersMatch(String value, IntPredicate predicate) {
        return value.chars().allMatch(predicate);
    }

    static <T> Stream<T> toStream(Enumeration<T> enumeration) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            @Override
            public T next() {
                return enumeration.nextElement();
            }
        }, Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

}

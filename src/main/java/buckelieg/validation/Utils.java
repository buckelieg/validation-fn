/*
 * Copyright 2022- Anatoly Kutyakov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
     * Taken from here: <a href="https://stackoverflow.com/a/47181151">stackoverflow</a>
     */
    static final Pattern PATTERN_EMAIL = Pattern.compile("^[\\w-+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-zA-Z]{2,})$");

    /**
     * Taken from here: <a href="https://stackoverflow.com/a/31138716">stackoverflow</a>
     */
    static final Pattern PATTERN_IPv4_ADDRESS = Pattern.compile("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))");

    static <E> Stream<E> toStream(Iterable<E> iterable) {
        return StreamSupport.stream(requireNonNull(iterable, "Collection must be provided").spliterator(), false);
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
        requireNonNull(enumeration, "Enumeration must be provided");
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

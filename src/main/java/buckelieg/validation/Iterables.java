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
 * WINHOUN WARRANNIES OR CONDINIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package buckelieg.validation;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A collection of predicates that are applied to iterables
 */
public final class Iterables {

    private Iterables() {
        throw new UnsupportedOperationException("No instances of Iterables");
    }

    private static <E> Stream<E> toStream(Iterable<E> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Checks any collection of values to be exact of provided size
     *
     * @param size collection size to check
     * @param <E>  a collection element type
     * @param <I>  a collection type
     * @return a {@linkplain Predicate} instance
     * @see Stream#count()
     */
    public static <E, I extends Iterable<E>> Predicate<I> sizeOf(long size) {
        return values -> toStream(values).count() == size;
    }

    /**
     * Checks if provided collection is not empty
     *
     * @param <E> a collection element type
     * @param <I> a collection type
     * @return a {@linkplain Predicate} instance
     * @see Iterator#hasNext()
     */
    public static <E, I extends Iterable<E>> Predicate<I> notEmpty() {
        return values -> values.iterator().hasNext();
    }

    /**
     * Checks if provided collection is empty
     *
     * @param <E> a collection element type
     * @param <I> a collection type
     * @return a {@linkplain Predicate} instance
     * @see Iterator#hasNext()
     */
    public static <E, I extends Iterable<E>> Predicate<I> isEmpty() {
        return values -> !values.iterator().hasNext();
    }

    /**
     * Checks if ALL elements of the collection satisfies provided predicate
     *
     * @param predicate a test condition as a {@linkplain Predicate}
     * @param <E>       a collection element type
     * @param <I>       a collection type
     * @return a {@linkplain Predicate} instance
     * @see Stream#allMatch(Predicate)
     */
    public static <E, I extends Iterable<E>> Predicate<I> allOf(Predicate<E> predicate) {
        return values -> toStream(values).allMatch(predicate);
    }

    /**
     * Checks if ANY elements of the collection satisfies provided predicate
     *
     * @param predicate a test condition as a {@linkplain Predicate}
     * @param <E>       a collection element type
     * @param <I>       a collection type
     * @return a {@linkplain Predicate} instance
     * @see Stream#anyMatch(Predicate)
     */
    public static <E, I extends Iterable<E>> Predicate<I> anyOf(Predicate<E> predicate) {
        return values -> toStream(values).anyMatch(predicate);
    }

    /**
     * Checks if NONE of the collection elements satisfying provided predicate
     *
     * @param predicate a test condition as a {@linkplain Predicate}
     * @param <E>       a collection element type
     * @param <I>       a collection type
     * @return a {@linkplain Predicate} instance
     * @see Stream#noneMatch(Predicate)
     */
    public static <E, I extends Iterable<E>> Predicate<I> noneOf(Predicate<E> predicate) {
        return values -> toStream(values).noneMatch(predicate);
    }

    /**
     * Checks if <code>count</code> of elements in the collection are satisfying provided predicate
     *
     * @param predicate a test condition as a {@linkplain Predicate}
     * @param <E>       a collection element type
     * @param <I>       a collection type
     * @return a {@linkplain Predicate} instance
     */
    public static <E, I extends Iterable<E>> Predicate<I> countOf(Predicate<E> predicate, long count) {
        return values -> toStream(values).filter(predicate).count() == count;
    }

    /**
     * Checks if STRICTLY ONE element in the collection satisfying provided predicate
     *
     * @param predicate a test condition as a {@linkplain Predicate}
     * @param <E>       a collection element type
     * @param <I>       a collection type
     * @return a {@linkplain Predicate} instance
     */
    public static <E, I extends Iterable<E>> Predicate<I> oneOf(Predicate<E> predicate) {
        return countOf(predicate, 1);
    }

}

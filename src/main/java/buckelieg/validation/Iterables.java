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

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static buckelieg.validation.Utils.toStream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * A collection of predicates that are applied to iterables
 */
public enum Iterables {

    ;

    /**
     * Checks any collection of values to be exact of provided size
     *
     * @param size collection size to check against
     * @param <E>  a collection element type
     * @param <I>  a collection type
     * @return a {@linkplain Predicate} instance
     * @throws IllegalArgumentException if <code>size</code> &lt; 0
     * @see Stream#count()
     */
    public static <E, I extends Iterable<E>> Predicate<I> sizeOf(long size) {
        if (size < 0) throw new IllegalArgumentException("Size must be greater that or equal to zero");
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
     * @throws NullPointerException if <code>predicate</code> is null
     * @see Stream#allMatch(Predicate)
     */
    public static <E, I extends Iterable<E>> Predicate<I> allOf(Predicate<E> predicate) {
        requireNonNull(predicate, "Predicate must be provided");
        return values -> toStream(values).allMatch(predicate);
    }

    /**
     * Checks if ALL elements of the collection satisfies provided another collection
     *
     * @param another another collection to test elements against
     * @param <E>     a collection element type
     * @param <I>     a collection type
     * @return a {@linkplain Predicate} instance
     * @throws NullPointerException if argument is null
     * @see Collection#contains(Object)
     */
    public static <E, I extends Iterable<E>> Predicate<I> allOf(I another) {
        requireNonNull(another, "Collection must be provided");
        Collection<E> allowedValues = toStream(another).collect(toList());
        return values -> toStream(values).allMatch(allowedValues::contains);
    }

    /**
     * Checks if ANY (one or more) elements of the collection satisfies provided predicate
     *
     * @param predicate a test condition as a {@linkplain Predicate}
     * @param <E>       a collection element type
     * @param <I>       a collection type
     * @return a {@linkplain Predicate} instance
     * @throws NullPointerException if <code>predicate</code> is null
     * @see Stream#anyMatch(Predicate)
     */
    public static <E, I extends Iterable<E>> Predicate<I> anyOf(Predicate<E> predicate) {
        requireNonNull(predicate, "Predicate must be provided");
        return values -> toStream(values).anyMatch(predicate);
    }

    /**
     * Checks if NONE of the collection elements satisfying provided predicate
     *
     * @param predicate a test condition as a {@linkplain Predicate}
     * @param <E>       a collection element type
     * @param <I>       a collection type
     * @return a {@linkplain Predicate} instance
     * @throws NullPointerException if <code>predicate</code> is null
     * @see Stream#noneMatch(Predicate)
     */
    public static <E, I extends Iterable<E>> Predicate<I> noneOf(Predicate<E> predicate) {
        requireNonNull(predicate, "Predicate must be provided");
        return values -> toStream(values).noneMatch(predicate);
    }

    /**
     * Checks if <code>count</code> of elements in the collection are satisfying provided predicate<br/>
     * This is a shortcut for:
     * <pre>{@code
     * countOf(predicate, Predicates.eq(count));
     * }</pre>
     *
     * @param predicate a test condition as a {@linkplain Predicate}
     * @param <E>       a collection element type
     * @param <I>       a collection type
     * @return a {@linkplain Predicate} instance
     * @throws NullPointerException     if <code>predicate</code> is null
     * @throws IllegalArgumentException if <code>count</code> &lt; 0
     * @see #countOf(Predicate, Predicate)
     */
    public static <E, I extends Iterable<E>> Predicate<I> countEq(Predicate<E> predicate, long count) {
        if (count < 0) throw new IllegalArgumentException("Count must be greater that or equal to zero");
        return countOf(predicate, Predicates.eq(count));
    }

    /**
     * Checks if <code>count</code> of elements in the collection are satisfying provided element predicate
     *
     * @param element an element test condition
     * @param count   a predicate to test count with
     * @param <E>     a collection element type
     * @param <I>     a collection type
     * @return a {@linkplain Predicate} instance
     * @throws NullPointerException if any argument is null
     */
    public static <E, I extends Iterable<E>> Predicate<I> countOf(Predicate<E> element, Predicate<Long> count) {
        requireNonNull(element, "Element predicate must be provided");
        requireNonNull(count, "Count predicate must be provided");
        return values -> count.test(toStream(values).filter(element).count());
    }

    /**
     * Checks if STRICTLY ONE element in the collection satisfying provided predicate
     *
     * @param predicate a test condition as a {@linkplain Predicate}
     * @param <E>       a collection element type
     * @param <I>       a collection type
     * @return a {@linkplain Predicate} instance
     * @throws NullPointerException if <code>predicate</code> is null
     */
    public static <E, I extends Iterable<E>> Predicate<I> oneOf(Predicate<E> predicate) {
        return countEq(predicate, 1);
    }

    /**
     * Checks if provided element is unique (e.q. collection of values contains this element at most once)
     *
     * @param value a collection element to check for uniqueness
     * @param <E>   a collection element type
     * @param <I>   a collection type
     * @return a {@linkplain Predicate} instance
     * @see #oneOf(Predicate)
     * @see Objects#equals(Object, Object)
     */
    public static <E, I extends Iterable<E>> Predicate<I> isUnique(E value) {
        return countOf(element -> Objects.equals(value, element), Predicates.le(1L));
    }

    /**
     * Checks if all elements in provided collection are unique<br/>
     * i.e. there are none of elements that conforms to <code>Objects.equals(e1, e2) == true</code>
     *
     * @param <E> a collection element type
     * @param <I> a collection type
     * @return a {@linkplain Predicate} instance
     */
    public static <E, I extends Iterable<E>> Predicate<I> allUnique() {
        return values -> {
            Collection<E> collection = toStream(values).collect(toList());
            return collection.size() == new HashSet<>(collection).size();
        };
    }

    /**
     * Checks if provided <code>value</code> is contained by a <code>filter</code> collection
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance which returns:<br/>true - if provided value is contained in a <code>filter</code> collection<br/>false - otherwise
     */
    public static <T> Predicate<T> in(Iterable<T> filter) {
        Collection<T> values = toStream(requireNonNull(filter, "Collection must be provided")).collect(toList());
        return values::contains;
    }

    /**
     * Checks if provided <code>value</code> belongs to specified values
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance which returns:<br/>true - if provided value is contained in a <code>filter</code> collection<br/>false - otherwise
     */
    @SafeVarargs
    public static <T> Predicate<T> in(T... filter) {
        T[] values = Arrays.copyOf(requireNonNull(filter, "Values must be provided"), filter.length);
        return value -> Arrays.asList(values).contains(value);
    }

    /**
     * Checks if provided <code>value</code> is contained by <code>filter</code> {@linkplain Stream} of values
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance which returns:<br/>true - if provided value is contained in a <code>filter</code> collection<br/>false - otherwise
     */
    public static <T> Predicate<T> in(Stream<T> filter) {
        return in(requireNonNull(filter, "Stream must be provided").collect(toList()));
    }

    /**
     * Checks whether provided element is contained by <code>filter</code> {@linkplain Enumeration} of values
     *
     * @param filter an enumerated value list
     * @param <T>    element type
     * @return a {@linkplain Predicate} instance
     */
    public static <T> Predicate<T> in(Enumeration<T> filter) {
        return in(toStream(filter));
    }

    /**
     * Checks if provided <code>value</code> is NOT contained by <code>filter</code> collection
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance which returns:<br/>true - if provided value is NOT contained in a <code>filter</code> collection<br/>false - otherwise
     */
    public static <T> Predicate<T> notIn(Iterable<T> filter) {
        Collection<T> values = toStream(requireNonNull(filter, "Collection must be provided")).collect(toList());
        return value -> !values.contains(value);
    }

    /**
     * Checks if provided <code>value</code> is NOT contained by <code>filter</code> stream of values
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance which returns:<br/>true - if provided value is NOT contained in a <code>filter</code> collection<br/>false - otherwise
     */
    public static <T> Predicate<T> notIn(Stream<T> filter) {
        return notIn(requireNonNull(filter, "Stream must be provided").collect(toList()));
    }

    /**
     * Checks if provided <code>value</code> is NOT contained in provided values
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance which returns:<br/>true - if provided value is NOT contained in a <code>filter</code> collection<br/>false - otherwise
     */
    @SafeVarargs
    public static <T> Predicate<T> notIn(T... filter) {
        T[] values = Arrays.copyOf(requireNonNull(filter, "Values must be provided"), filter.length);
        return value -> Stream.of(values).noneMatch(v -> Objects.equals(value, v));
    }

    /**
     * Checks whether provided element is NOT contained by <code>filter</code> {@linkplain Enumeration} of values
     *
     * @param filter an enumerated value list
     * @param <T>    element type
     * @return a {@linkplain Predicate} instance
     */
    public static <T> Predicate<T> notIn(Enumeration<T> filter) {
        return notIn(toStream(filter));
    }

}

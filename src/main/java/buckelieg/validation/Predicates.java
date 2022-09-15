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
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static buckelieg.validation.Utils.toStream;
import static java.util.Objects.requireNonNull;

/**
 * A collection of general purpose predicates with the basic checks
 */
public final class Predicates {

    private Predicates() {
        throw new UnsupportedOperationException("No instances of Predicates");
    }

    /**
     * Converts provided lambda function to a {@linkplain Predicate} reference
     *
     * @param predicate a predicate to obtain reference to (must not be null)
     * @param <T>       a value type
     * @return a reference to {@linkplain Predicate}
     * @throws NullPointerException if <code>predicate</code> is null
     */
    public static <T> Predicate<T> of(Predicate<T> predicate) {
        return requireNonNull(predicate, "Predicate must be provided");
    }

    /**
     * Checks if provided <code>value</code> is GREATER THAN the <code>measure</code> value
     *
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> gt(T measure) {
        return value -> value.compareTo(measure) > 0;
    }

    /**
     * Checks if provided <code>value</code> is LESS THAN the <code>measure</code> value
     *
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> lt(T measure) {
        return value -> value.compareTo(measure) < 0;
    }

    /**
     * Checks if provided <code>value</code> is EQUAL to the <code>measure</code> value
     *
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> eq(T measure) {
        return value -> value.compareTo(measure) == 0;
    }

    /**
     * Checks if provided <code>value</code> is GREATER THAN OR EQUAL to the <code>measure</code> value
     *
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> ge(T measure) {
        return value -> value.compareTo(measure) >= 0;
    }

    /**
     * Checks if provided <code>value</code> is LESS THAN OR EQUAL to the <code>measure</code> value
     *
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> le(T measure) {
        return value -> value.compareTo(measure) <= 0;
    }

    /**
     * Checks if provided <code>value</code> is contained by a <code>filter</code> collection
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance which returns:<br/>true - if provided value is contained in a <code>filter</code> collection<br/>false - otherwise
     */
    public static <T extends Comparable<T>> Predicate<T> in(Collection<T> filter) {
        return filter::contains;
    }

    /**
     * Checks if provided <code>value</code> belongs to specified values
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance which returns:<br/>true - if provided value is contained in a <code>filter</code> collection<br/>false - otherwise
     */
    @SafeVarargs
    public static <T extends Comparable<T>> Predicate<T> in(T... filter) {
        return value -> Arrays.asList(filter).contains(value);
    }

    /**
     * Checks if provided <code>value</code> is contained by <code>filter</code> {@linkplain Stream} of values
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance which returns:<br/>true - if provided value is contained in a <code>filter</code> collection<br/>false - otherwise
     */
    public static <T extends Comparable<T>> Predicate<T> in(Stream<T> filter) {
        return value -> filter.anyMatch(v -> Objects.equals(value, v));
    }

    /**
     * Checks whether provided element is contained by <code>filter</code> {@linkplain Enumeration} of values
     *
     * @param filter an enumerated value list
     * @param <T>    element type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> in(Enumeration<T> filter) {
        return in(toStream(filter));
    }

    /**
     * Checks if provided <code>value</code> is NOT contained by <code>filter</code> collection
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance which returns:<br/>true - if provided value is NOT contained in a <code>filter</code> collection<br/>false - otherwise
     */
    public static <T extends Comparable<T>> Predicate<T> notIn(Collection<T> filter) {
        return value -> filter.stream().noneMatch(v -> Objects.equals(value, v));
    }

    /**
     * Checks if provided <code>value</code> is NOT contained by <code>filter</code> stream of values
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance which returns:<br/>true - if provided value is NOT contained in a <code>filter</code> collection<br/>false - otherwise
     */
    public static <T extends Comparable<T>> Predicate<T> notIn(Stream<T> filter) {
        return value -> filter.noneMatch(v -> Objects.equals(value, v));
    }

    /**
     * Checks if provided <code>value</code> is NOT contained in provided values
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance which returns:<br/>true - if provided value is NOT contained in a <code>filter</code> collection<br/>false - otherwise
     */
    @SafeVarargs
    public static <T extends Comparable<T>> Predicate<T> notIn(T... filter) {
        return value -> Stream.of(filter).noneMatch(v -> Objects.equals(value, v));
    }

    /**
     * Checks whether provided element is NOT contained by <code>filter</code> {@linkplain Enumeration} of values
     *
     * @param filter an enumerated value list
     * @param <T>    element type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> notIn(Enumeration<T> filter) {
        return notIn(toStream(filter));
    }

}

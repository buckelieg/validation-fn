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

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
     * @param value   a validated value
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return true - if provided <code>value</code> is greater than <code>measure</code><br/>false - otherwise
     */
    public static <T extends Comparable<T>> boolean gt(T value, T measure) {
        return value.compareTo(measure) > 0;
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Predicates#gt(Comparable, Comparable)} method
     *
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> gt(T measure) {
        return value -> gt(value, measure);
    }

    /**
     * Checks if provided <code>value</code> is LESS THAN the <code>measure</code> value
     *
     * @param value   a validated value
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return true - if provided value is LESS THAN the <code>measure</code> one<br/>false - otherwise
     */
    public static <T extends Comparable<T>> boolean lt(T value, T measure) {
        return value.compareTo(measure) < 0;
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Predicates#lt(Comparable, Comparable)} method
     *
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> lt(T measure) {
        return value -> lt(value, measure);
    }

    /**
     * Checks if provided <code>value</code> is EQUAL to the <code>measure</code> value
     *
     * @param value   a validated value
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return true - if provided value is EQUAL to the <code>measure</code> one<br/>false - otherwise
     */
    public static <T extends Comparable<T>> boolean eq(T value, T measure) {
        return value.compareTo(measure) == 0;
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Predicates#eq(Comparable, Comparable)} method
     *
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> eq(T measure) {
        return value -> eq(value, measure);
    }

    /**
     * Checks if provided <code>value</code> is GREATER THAN OR EQUAL to the <code>measure</code> value
     *
     * @param value   a validated value
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return true - if provided value is GREATER OR EQUAL to the <code>measure</code> one<br/>false - otherwise
     */
    public static <T extends Comparable<T>> boolean ge(T value, T measure) {
        return value.compareTo(measure) >= 0;
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Predicates#ge(Comparable, Comparable)} method
     *
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> ge(T measure) {
        return value -> ge(value, measure);
    }

    /**
     * Checks if provided <code>value</code> is LESS THAN OR EQUAL to the <code>measure</code> value
     *
     * @param value   a validated value
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return true - if provided value is LESS OR EQUAL to the <code>measure</code> one<br/>false - otherwise
     */
    public static <T extends Comparable<T>> boolean le(T value, T measure) {
        return value.compareTo(measure) <= 0;
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Predicates#le(Comparable, Comparable)} method
     *
     * @param measure a value the validated value is validated against
     * @param <T>     a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> le(T measure) {
        return value -> le(value, measure);
    }


    /**
     * Checks if provided <code>value</code> is contained by a <code>filter</code> collection
     *
     * @param value  a validated value
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return true - if provided value is contained in a <code>filter</code> collection<br/>false - otherwise
     */
    public static <T> boolean in(T value, Collection<T> filter) {
        return in(value, filter.stream());
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Predicates#in(Object, Collection)} method
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance
     * @see Predicates#in(Object, Collection)
     */
    public static <T extends Comparable<T>> Predicate<T> in(Collection<T> filter) {
        return value -> in(value, filter);
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Predicates#in(Stream)} method
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance
     */
    @SafeVarargs
    public static <T extends Comparable<T>> Predicate<T> in(T... filter) {
        return value -> in(value, Stream.of(filter));
    }


    /**
     * Checks if provided <code>value</code> is contained by <code>filter</code> {@linkplain Stream} of values
     *
     * @param value  a validated value
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return true - if provided value is contained in a <code>filter</code> collection<br/>false - otherwise
     */
    public static <T> boolean in(T value, Stream<T> filter) {
        return filter.anyMatch(value::equals);
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Predicates#in(Object, Stream)} method
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> in(Stream<T> filter) {
        return value -> in(value, filter);
    }

    /**
     * Checks if provided <code>value</code> is contained by <code>filter</code> {@linkplain Collection} of values
     *
     * @param value  a validated value
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return true - if provided value is NOT contained in a <code>filter</code> collection<br/>false - otherwise
     */
    public static <T> boolean notIn(T value, Collection<T> filter) {
        return notIn(value, filter.stream());
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Predicates#in(Object, Collection)} method
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> notIn(Collection<T> filter) {
        return value -> notIn(value, filter);
    }

    /**
     * Checks if provided <code>value</code> is NOT contained by <code>filter</code> stream of values
     *
     * @param value  a validated value
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return true - if provided value is NOT contained in a <code>filter</code> collection<br/>false - otherwise
     */
    public static <T> boolean notIn(T value, Stream<T> filter) {
        return filter.noneMatch(value::equals);
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Predicates#notIn(Object, Stream)} method
     *
     * @param filter a collection of values to be validated value validated against
     * @param <T>    a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> notIn(Stream<T> filter) {
        return value -> notIn(value, filter);
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Predicates#notIn(Object, Stream)} method
     *
     * @param filters a collection of values to be validated value validated against
     * @param <T>     a value type
     * @return a {@linkplain Predicate} instance
     */
    @SafeVarargs
    public static <T extends Comparable<T>> Predicate<T> notIn(T... filters) {
        return value -> notIn(value, Stream.of(filters));
    }
}

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

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A collection of number-related predicates
 */
public final class Numbers {

    private Numbers() {
        throw new UnsupportedOperationException("No instances of Numbers");
    }

    private static BigDecimal toBigDecimal(Object value) {
        return new BigDecimal(value.toString());
    }

    /**
     * A {@linkplain Predicate} that checks provided value to be a number
     *
     * @param value a validated value
     * @return true - if provided value is number, false - otherwise
     */
    public static boolean isNumber(Object value) {
        try {
            toBigDecimal(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Numbers#isNumber(Object)} method
     *
     * @param <N> value type
     * @return a {@linkplain Predicate} instance
     */
    public static <N extends Number & Comparable<N>> Predicate<N> isNumber() {
        return Numbers::isNumber;
    }

    /**
     * Tests provided number whether it is equal to zero
     *
     * @param value a validated value
     * @return true - if provided value equals to zero, false - otherwise
     * @see BigDecimal#signum()
     */
    public static <N extends Number & Comparable<N>> boolean isZero(N value) {
        return toBigDecimal(value).signum() == 0;
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Numbers#isZero(Number)} method
     *
     * @param <N> value type
     * @return a {@linkplain Predicate} instance
     */
    public static <N extends Number & Comparable<N>> Predicate<N> isZero() {
        return Numbers::isZero;
    }

    /**
     * Tests provided number whether it is positive
     *
     * @param value a validated value
     * @param <N>   a value type
     * @return true - if provided value is strictly greater than zero, false - otherwise
     * @see BigDecimal#signum()
     */
    public static <N extends Number & Comparable<N>> boolean isPositive(N value) {
        return toBigDecimal(value).signum() > 0;
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Numbers#isPositive(Number)} method
     *
     * @param <N> value type
     * @return a {@linkplain Predicate} instance
     */
    public static <N extends Number & Comparable<N>> Predicate<N> isPositive() {
        return Numbers::isPositive;
    }

    /**
     * Tests provided number whether it is negative
     *
     * @param value a validated value
     * @param <N>   a value type
     * @return true - if provided number is strictly lesser than zero, false - otherwise
     * @see BigDecimal#signum()
     */
    public static <N extends Number & Comparable<N>> boolean isNegative(N value) {
        return toBigDecimal(value).signum() < 0;
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Numbers#isNegative(Number)} method
     *
     * @param <N> value type
     * @return a {@linkplain Predicate} instance
     */
    public static <N extends Number & Comparable<N>> Predicate<N> isNegative() {
        return Numbers::isNegative;
    }

    /**
     * Check provided number to be greater than provided <code>minimum</code>
     *
     * @param minimum a minimum allowed number
     * @param <N>     a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <N extends Number & Comparable<N>> Predicate<N> min(N minimum) {
        return Predicates.lt(minimum);
    }

    /**
     * Check provided number to be less than provided <code>maximum</code>
     *
     * @param maximum a maximum allowed number
     * @param <N>     a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <N extends Number & Comparable<N>> Predicate<N> max(N maximum) {
        return Predicates.gt(maximum);
    }

    /**
     * Check if this {@linkplain BigDecimal} value scale is EQUAL TO provided number
     *
     * @param measure a scale to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#scale()
     */
    public static Predicate<BigDecimal> isScaleEq(int measure) {
        return isMeasuredAt(BigDecimal::scale, Predicates.eq(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value scale is LESS THAN provided number
     *
     * @param measure a scale to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#scale()
     */
    public static Predicate<BigDecimal> isScaleLt(int measure) {
        return isMeasuredAt(BigDecimal::scale, Predicates.lt(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value scale is LESS THAN OR EQUAL TO provided number
     *
     * @param measure a scale to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#scale()
     */
    public static Predicate<BigDecimal> isScaleLe(int measure) {
        return isMeasuredAt(BigDecimal::scale, Predicates.le(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value scale is GREATER THAN provided number
     *
     * @param measure a scale to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#scale()
     */
    public static Predicate<BigDecimal> isScaleGt(int measure) {
        return isMeasuredAt(BigDecimal::scale, Predicates.gt(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value scale is GREATER THAN OR EQUAL TO provided number
     *
     * @param measure a scale to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#scale()
     */
    public static Predicate<BigDecimal> isScaleGe(int measure) {
        return isMeasuredAt(BigDecimal::scale, Predicates.ge(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value precision is EQUAL TO provided number
     *
     * @param measure a precision to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#precision()
     */
    public static Predicate<BigDecimal> isPrecisionEq(int measure) {
        return isMeasuredAt(BigDecimal::precision, Predicates.eq(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value precision is LESS THAN provided number
     *
     * @param measure a precision to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#precision()
     */
    public static Predicate<BigDecimal> isPrecisionLt(int measure) {
        return isMeasuredAt(BigDecimal::precision, Predicates.lt(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value precision is LESS THAN OR EQUAL TO provided number
     *
     * @param measure a precision to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#precision()
     */
    public static Predicate<BigDecimal> isPrecisionLe(int measure) {
        return isMeasuredAt(BigDecimal::precision, Predicates.le(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value precision is GREATER THAN provided number
     *
     * @param measure a precision to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#precision()
     */
    public static Predicate<BigDecimal> isPrecisionGt(int measure) {
        return isMeasuredAt(BigDecimal::precision, Predicates.gt(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value precision is GREATER THAN OR EQUAL TO provided number
     *
     * @param measure a precision to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#precision()
     */
    public static Predicate<BigDecimal> isPrecisionGe(int measure) {
        return isMeasuredAt(BigDecimal::precision, Predicates.ge(measure));
    }

    private static Predicate<BigDecimal> isMeasuredAt(Function<BigDecimal, Integer> mapper, Predicate<Integer> predicate) {
        return value -> predicate.test(mapper.apply(value));
    }
}

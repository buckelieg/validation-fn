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
import java.util.function.Predicate;

import static buckelieg.validation.Utils.isMeasuredAt;

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
     * Check if this {@linkplain BigDecimal} value scale conforms provided predicate
     *
     * @param predicate a predicate to test scale with
     * @return a {@linkplain Predicate} instance
     * @throws NullPointerException if <code>predicate</code> is null
     * @see BigDecimal#scale()
     */
    public static Predicate<BigDecimal> isScaleOf(Predicate<Integer> predicate) {
        return isMeasuredAt(BigDecimal::scale, predicate);
    }

    /**
     * Check if this {@linkplain BigDecimal} value scale is EQUAL TO provided number
     *
     * @param measure a scale to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#scale()
     * @see Predicates#eq(Comparable)
     */
    public static Predicate<BigDecimal> isScaleEq(int measure) {
        return isScaleOf(Predicates.eq(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value scale is LESS THAN provided number
     *
     * @param measure a scale to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#scale()
     * @see Predicates#lt(Comparable)
     */
    public static Predicate<BigDecimal> isScaleLt(int measure) {
        return isScaleOf(Predicates.lt(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value scale is LESS THAN OR EQUAL TO provided number
     *
     * @param measure a scale to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#scale()
     * @see Predicates#le(Comparable)
     */
    public static Predicate<BigDecimal> isScaleLe(int measure) {
        return isScaleOf(Predicates.le(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value scale is GREATER THAN provided number
     *
     * @param measure a scale to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#scale()
     * @see Predicates#gt(Comparable)
     */
    public static Predicate<BigDecimal> isScaleGt(int measure) {
        return isScaleOf(Predicates.gt(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value scale is GREATER THAN OR EQUAL TO provided number
     *
     * @param measure a scale to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#scale()
     * @see Predicates#ge(Comparable)
     */
    public static Predicate<BigDecimal> isScaleGe(int measure) {
        return isScaleOf(Predicates.ge(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value precision conforms provided predicate
     *
     * @param predicate a predicate to test precision with
     * @return a {@linkplain Predicate} instance
     * @throws NullPointerException if <code>predicate</code> is null
     * @see BigDecimal#precision()
     */
    public static Predicate<BigDecimal> isPrecisionOf(Predicate<Integer> predicate) {
        return isMeasuredAt(BigDecimal::precision, predicate);
    }

    /**
     * Check if this {@linkplain BigDecimal} value precision is EQUAL TO provided number
     *
     * @param measure a precision to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#precision()
     * @see Predicates#eq(Comparable)
     */
    public static Predicate<BigDecimal> isPrecisionEq(int measure) {
        return isPrecisionOf(Predicates.eq(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value precision is LESS THAN provided number
     *
     * @param measure a precision to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#precision()
     * @see Predicates#lt(Comparable)
     */
    public static Predicate<BigDecimal> isPrecisionLt(int measure) {
        return isPrecisionOf(Predicates.lt(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value precision is LESS THAN OR EQUAL TO provided number
     *
     * @param measure a precision to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#precision()
     * @see Predicates#le(Comparable)
     */
    public static Predicate<BigDecimal> isPrecisionLe(int measure) {
        return isPrecisionOf(Predicates.le(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value precision is GREATER THAN provided number
     *
     * @param measure a precision to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#precision()
     * @see Predicates#gt(Comparable)
     */
    public static Predicate<BigDecimal> isPrecisionGt(int measure) {
        return isPrecisionOf(Predicates.gt(measure));
    }

    /**
     * Check if this {@linkplain BigDecimal} value precision is GREATER THAN OR EQUAL TO provided number
     *
     * @param measure a precision to compare to
     * @return a {@linkplain Predicate} instance
     * @see BigDecimal#precision()
     * @see Predicates#ge(Comparable)
     */
    public static Predicate<BigDecimal> isPrecisionGe(int measure) {
        return isPrecisionOf(Predicates.ge(measure));
    }

}

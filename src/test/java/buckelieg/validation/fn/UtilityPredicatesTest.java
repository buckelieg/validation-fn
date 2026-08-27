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
package buckelieg.validation.fn;

import buckelieg.validation.Maps;
import buckelieg.validation.Numbers;
import buckelieg.validation.Predicates;
import buckelieg.validation.Ranges;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class UtilityPredicatesTest {

    @Test
    public void generalPredicatesCoverConstantsComparisonsAndIdentity() {
        Predicate<String> supplied = value -> value.length() > 2;
        assertSame(supplied, Predicates.of(supplied));
        assertTrue(Predicates.TRUE.<String>predicate().test(null));
        assertFalse(Predicates.FALSE.<String>predicate().test("value"));

        assertTrue(Predicates.gt(3).test(4));
        assertFalse(Predicates.gt(3).test(3));
        assertTrue(Predicates.lt(3).test(2));
        assertFalse(Predicates.lt(3).test(3));
        assertTrue(Predicates.eq(3).test(3));
        assertFalse(Predicates.eq(3).test(4));
        assertTrue(Predicates.ge(3).test(3));
        assertFalse(Predicates.ge(3).test(2));
        assertTrue(Predicates.le(3).test(3));
        assertFalse(Predicates.le(3).test(4));

        assertThrows(NullPointerException.class, () -> Predicates.of(null));
        assertThrows(NullPointerException.class, () -> Predicates.lt(null));
        assertThrows(NullPointerException.class, () -> Predicates.eq(null));
        assertThrows(NullPointerException.class, () -> Predicates.ge(null));
        assertThrows(NullPointerException.class, () -> Predicates.le(null));
    }

    @Test
    public void membershipPredicatesCoverEveryInputShapeAndSnapshotArrays() {
        assertTrue(Predicates.in(Arrays.asList("a", "b")).test("a"));
        assertFalse(Predicates.in(Arrays.asList("a", "b")).test("c"));
        assertTrue(Predicates.notIn(Arrays.asList("a", null)).test("b"));
        assertFalse(Predicates.notIn(Arrays.asList("a", null)).test(null));

        String[] included = {"a", "b"};
        Predicate<String> inArray = Predicates.in(included);
        included[0] = "changed";
        assertTrue(inArray.test("a"));
        assertFalse(inArray.test("changed"));

        String[] excluded = {"a", "b"};
        Predicate<String> notInArray = Predicates.notIn(excluded);
        excluded[0] = "changed";
        assertFalse(notInArray.test("a"));
        assertTrue(notInArray.test("changed"));

        assertTrue(Predicates.in(Stream.of("a", "b")).test("b"));
        assertTrue(Predicates.notIn(Stream.of("a", "b")).test("c"));
        Enumeration<String> enumeration = Collections.enumeration(Arrays.asList("a", "b"));
        assertTrue(Predicates.in(enumeration).test("a"));
        Enumeration<String> otherEnumeration = Collections.enumeration(Arrays.asList("a", "b"));
        assertTrue(Predicates.notIn(otherEnumeration).test("c"));

        assertThrows(NullPointerException.class, () -> Predicates.in((java.util.Collection<Object>) null));
        assertThrows(NullPointerException.class, () -> Predicates.notIn((Object[]) null));
        assertThrows(NullPointerException.class, () -> Predicates.in((Stream<Object>) null));
        assertThrows(NullPointerException.class, () -> Predicates.notIn((Enumeration<Object>) null));
    }

    @Test
    public void numberPredicatesCoverSignsAndBigDecimalMeasures() {
        assertTrue(Numbers.isNumber(12));
        assertTrue(Numbers.isNumber("12.50"));
        assertFalse(Numbers.isNumber("not-a-number"));
        assertFalse(Numbers.isNumber(null));
        assertTrue(Numbers.<Integer>isNumber().test(12));

        assertTrue(Numbers.isZero(0));
        assertFalse(Numbers.isZero(1));
        assertTrue(Numbers.<Integer>isZero().test(0));
        assertTrue(Numbers.isPositive(1));
        assertFalse(Numbers.isPositive(0));
        assertTrue(Numbers.<Integer>isPositive().test(1));
        assertTrue(Numbers.isNegative(-1));
        assertFalse(Numbers.isNegative(0));
        assertTrue(Numbers.<Integer>isNegative().test(-1));

        BigDecimal number = new BigDecimal("123.45");
        assertTrue(Numbers.isScaleOf(scale -> scale == 2).test(number));
        assertTrue(Numbers.isScaleEq(2).test(number));
        assertTrue(Numbers.isScaleLt(3).test(number));
        assertTrue(Numbers.isScaleLe(2).test(number));
        assertTrue(Numbers.isScaleGt(1).test(number));
        assertTrue(Numbers.isScaleGe(2).test(number));
        assertTrue(Numbers.isPrecisionOf(precision -> precision == 5).test(number));
        assertTrue(Numbers.isPrecisionEq(5).test(number));
        assertTrue(Numbers.isPrecisionLt(6).test(number));
        assertTrue(Numbers.isPrecisionLe(5).test(number));
        assertTrue(Numbers.isPrecisionGt(4).test(number));
        assertTrue(Numbers.isPrecisionGe(5).test(number));

        assertThrows(NullPointerException.class, () -> Numbers.isScaleOf(null));
        assertThrows(NullPointerException.class, () -> Numbers.isPrecisionOf(null));
    }

    @Test
    public void mapPredicatesCoverPresenceSizeAndMappedValues() {
        Map<String, Integer> values = new HashMap<>();
        assertTrue(Maps.<String, Integer>isEmpty().test(values));
        values.put("answer", 42);

        assertTrue(Maps.<String, Integer>containsKey("answer").test(values));
        assertFalse(Maps.<String, Integer>containsKey("missing").test(values));
        assertTrue(Maps.<String, Integer>containsValue(42).test(values));
        assertFalse(Maps.<String, Integer>containsValue(7).test(values));
        assertFalse(Maps.<String, Integer>isEmpty().test(values));
        assertTrue(Maps.<String, Integer>sizeOf(1).test(values));
        assertFalse(Maps.<String, Integer>sizeOf(2).test(values));
        assertTrue(Maps.<String, Integer>keyValue("answer", value -> value > 40).test(values));
        assertFalse(Maps.<String, Integer>keyValue("answer", value -> value < 40).test(values));
        assertTrue(Maps.<String, Integer>keyValueEquals("answer", 42).test(values));
        assertTrue(Maps.<String, Integer>keyValueEquals("missing", null).test(values));

        assertThrows(IllegalArgumentException.class, () -> Maps.sizeOf(-1));
        assertThrows(NullPointerException.class, () -> Maps.keyValue("answer", null));
    }

    @Test
    public void rangesCoverInclusiveExclusiveAndInvalidBoundaries() {
        assertTrue(Ranges.inside(1, 3).test(1));
        assertTrue(Ranges.inside(1, 3).test(3));
        assertFalse(Ranges.inside(1, 3).test(4));
        assertTrue(Ranges.strictInside(1, 3).test(2));
        assertFalse(Ranges.strictInside(1, 3).test(1));
        assertTrue(Ranges.outside(1, 3).test(0));
        assertTrue(Ranges.outside(1, 3).test(4));
        assertFalse(Ranges.outside(1, 3).test(2));

        assertThrows(NullPointerException.class, () -> Ranges.inside(null, 3));
        assertThrows(NullPointerException.class, () -> Ranges.outside(1, null));
        assertThrows(IllegalArgumentException.class, () -> Ranges.strictInside(3, 1));
    }
}

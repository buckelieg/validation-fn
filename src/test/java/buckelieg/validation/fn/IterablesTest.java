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

import buckelieg.validation.Iterables;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class IterablesTest {

    @Test
    public void sizeAndEmptinessPredicatesHandleEmptyAndPopulatedValues() {
        List<Integer> empty = Collections.emptyList();
        List<Integer> values = Arrays.asList(1, 2, 3);

        assertTrue(Iterables.<Integer, List<Integer>>sizeOf(0).test(empty));
        assertTrue(Iterables.<Integer, List<Integer>>sizeOf(3).test(values));
        assertFalse(Iterables.<Integer, List<Integer>>sizeOf(2).test(values));
        assertTrue(Iterables.<Integer, List<Integer>>isEmpty().test(empty));
        assertFalse(Iterables.<Integer, List<Integer>>isEmpty().test(values));
        assertFalse(Iterables.<Integer, List<Integer>>notEmpty().test(empty));
        assertTrue(Iterables.<Integer, List<Integer>>notEmpty().test(values));
        assertThrows(IllegalArgumentException.class, () -> Iterables.sizeOf(-1));
    }

    @Test
    public void quantifiedPredicatesCoverAllAnyNoneAndAllowedValues() {
        List<Integer> values = Arrays.asList(2, 4, 5);

        assertFalse(Iterables.<Integer, List<Integer>>allOf(value -> value % 2 == 0).test(values));
        assertTrue(Iterables.<Integer, List<Integer>>allOf(value -> value > 0).test(values));
        assertTrue(Iterables.<Integer, List<Integer>>anyOf(value -> value % 2 != 0).test(values));
        assertFalse(Iterables.<Integer, List<Integer>>anyOf(value -> value < 0).test(values));
        assertTrue(Iterables.<Integer, List<Integer>>noneOf(value -> value < 0).test(values));
        assertFalse(Iterables.<Integer, List<Integer>>noneOf(value -> value == 4).test(values));

        Predicate<List<Integer>> allowed = Iterables.allOf(Arrays.asList(2, 4, 5, 6));
        assertTrue(allowed.test(values));
        assertFalse(allowed.test(Arrays.asList(2, 7)));

        assertThrows(NullPointerException.class, () -> Iterables.allOf((Predicate<Object>) null));
        assertThrows(NullPointerException.class, () -> Iterables.anyOf(null));
        assertThrows(NullPointerException.class, () -> Iterables.noneOf(null));
        assertThrows(NullPointerException.class, () -> Iterables.allOf((Iterable<Object>) null));
    }

    @Test
    public void countingAndUniquenessPredicatesCoverBoundaryCounts() {
        List<Integer> values = Arrays.asList(1, 2, 2, 3);

        assertTrue(Iterables.<Integer, List<Integer>>countEq(value -> value == 2, 2).test(values));
        assertFalse(Iterables.<Integer, List<Integer>>countEq(value -> value == 2, 1).test(values));
        assertTrue(Iterables.<Integer, List<Integer>>countOf(value -> value > 1, count -> count == 3).test(values));
        assertTrue(Iterables.<Integer, List<Integer>>oneOf(value -> value == 1).test(values));
        assertFalse(Iterables.<Integer, List<Integer>>oneOf(value -> value == 2).test(values));
        assertTrue(Iterables.<Integer, List<Integer>>isUnique(1).test(values));
        assertFalse(Iterables.<Integer, List<Integer>>isUnique(2).test(values));
        assertFalse(Iterables.<Integer, List<Integer>>allUnique().test(values));
        assertTrue(Iterables.<Integer, List<Integer>>allUnique().test(Arrays.asList(1, 2, 3)));

        assertThrows(IllegalArgumentException.class, () -> Iterables.countEq(value -> true, -1));
        assertThrows(NullPointerException.class, () -> Iterables.countOf(null, count -> true));
        assertThrows(NullPointerException.class, () -> Iterables.countOf(value -> true, null));
    }

    @Test
    public void membershipPredicatesCoverIterableArrayStreamAndEnumerationInputs() {
        Predicate<String> iterableIn = Iterables.in(Arrays.asList("a", "b"));
        Predicate<String> iterableNotIn = Iterables.notIn(Arrays.asList("a", "b"));
        assertTrue(iterableIn.test("a"));
        assertFalse(iterableIn.test("c"));
        assertFalse(iterableNotIn.test("a"));
        assertTrue(iterableNotIn.test("c"));

        String[] included = {"a", "b"};
        Predicate<String> arrayIn = Iterables.in(included);
        included[0] = "changed";
        assertTrue(arrayIn.test("a"));
        assertFalse(arrayIn.test("changed"));

        String[] excluded = {"a", "b"};
        Predicate<String> arrayNotIn = Iterables.notIn(excluded);
        excluded[0] = "changed";
        assertFalse(arrayNotIn.test("a"));
        assertTrue(arrayNotIn.test("changed"));

        assertTrue(Iterables.in(Stream.of("a", "b")).test("b"));
        assertTrue(Iterables.notIn(Stream.of("a", "b")).test("c"));
        Enumeration<String> includedEnumeration = Collections.enumeration(Arrays.asList("a", "b"));
        Enumeration<String> excludedEnumeration = Collections.enumeration(Arrays.asList("a", "b"));
        assertTrue(Iterables.in(includedEnumeration).test("a"));
        assertTrue(Iterables.notIn(excludedEnumeration).test("c"));
    }

    @Test
    public void membershipFactoriesRejectNullSources() {
        assertThrows(NullPointerException.class, () -> Iterables.in((Iterable<Object>) null));
        assertThrows(NullPointerException.class, () -> Iterables.notIn((Iterable<Object>) null));
        assertThrows(NullPointerException.class, () -> Iterables.in((Object[]) null));
        assertThrows(NullPointerException.class, () -> Iterables.notIn((Object[]) null));
        assertThrows(NullPointerException.class, () -> Iterables.in((Stream<Object>) null));
        assertThrows(NullPointerException.class, () -> Iterables.notIn((Enumeration<Object>) null));
    }
}

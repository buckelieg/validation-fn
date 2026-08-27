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
import buckelieg.validation.Predicates;
import buckelieg.validation.Ranges;
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

public class HelperPredicatesTest {

    @Test
    public void outsideMatchesValuesBeyondEitherBoundary() {
        Predicate<Integer> outside = Ranges.outside(1, 10);

        assertTrue(outside.test(0));
        assertTrue(outside.test(11));
        assertFalse(outside.test(1));
        assertFalse(outside.test(5));
        assertFalse(outside.test(10));
    }

    @Test
    public void rangesRejectReversedBoundaries() {
        assertThrows(IllegalArgumentException.class, () -> Ranges.inside(10, 1));
        assertThrows(IllegalArgumentException.class, () -> Ranges.outside(10, 1));
        assertThrows(IllegalArgumentException.class, () -> Ranges.strictInside(10, 1));
    }

    @Test
    public void allOfRequiresEveryValueToBeAllowed() {
        Predicate<List<Integer>> allOf = Iterables.allOf(Arrays.asList(1, 3));

        assertTrue(allOf.test(Arrays.asList(1, 3)));
        assertTrue(allOf.test(Collections.singletonList(1)));
        assertTrue(allOf.test(Collections.emptyList()));
        assertFalse(allOf.test(Arrays.asList(1, 2)));
    }

    @Test
    public void streamPredicatesCanBeReused() {
        Predicate<Integer> in = Predicates.in(Stream.of(1, 2));
        Predicate<Integer> notIn = Predicates.notIn(Stream.of(1, 2));

        assertTrue(in.test(1));
        assertTrue(in.test(2));
        assertFalse(in.test(3));
        assertFalse(notIn.test(1));
        assertFalse(notIn.test(2));
        assertTrue(notIn.test(3));
    }

    @Test
    public void enumerationPredicatesCanBeReused() {
        Enumeration<Integer> included = Collections.enumeration(Arrays.asList(1, 2));
        Enumeration<Integer> excluded = Collections.enumeration(Arrays.asList(1, 2));
        Predicate<Integer> in = Predicates.in(included);
        Predicate<Integer> notIn = Predicates.notIn(excluded);

        assertTrue(in.test(1));
        assertTrue(in.test(2));
        assertFalse(in.test(3));
        assertFalse(notIn.test(1));
        assertFalse(notIn.test(2));
        assertTrue(notIn.test(3));
    }

    @Test
    public void iterableStreamPredicatesCanBeReused() {
        Predicate<Integer> in = Iterables.in(Stream.of(1, 2));
        Predicate<Integer> notIn = Iterables.notIn(Stream.of(1, 2));

        assertTrue(in.test(1));
        assertTrue(in.test(2));
        assertFalse(in.test(3));
        assertFalse(notIn.test(1));
        assertFalse(notIn.test(2));
        assertTrue(notIn.test(3));
    }

    @Test
    public void uniqueMeansAtMostOneOccurrence() {
        Predicate<List<Integer>> unique = Iterables.isUnique(1);

        assertTrue(unique.test(Collections.emptyList()));
        assertTrue(unique.test(Arrays.asList(1, 2)));
        assertFalse(unique.test(Arrays.asList(1, 1, 2)));
    }
}

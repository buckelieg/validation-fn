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

import buckelieg.validation.Strings;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class StringsTest {

    private enum Value {
        FIRST,
        SECOND
    }

    @Test
    public void enumAndSubstringPredicatesCoverAllQuantifiers() {
        assertTrue(Strings.in(Value.class).test("FIRST"));
        assertFalse(Strings.in(Value.class).test("first"));
        assertFalse(Strings.in(Value.class).test(null));

        assertTrue(Strings.containsAll("alpha", "beta").test("alpha-beta"));
        assertFalse(Strings.containsAll("alpha", "gamma").test("alpha-beta"));
        assertTrue(Strings.containsAllIgnoreCase("ALPHA", "Beta").test("alpha-beta"));
        assertFalse(Strings.containsAllIgnoreCase("alpha", "gamma").test("ALPHA-BETA"));

        assertTrue(Strings.containsNone("gamma", "delta").test("alpha-beta"));
        assertFalse(Strings.containsNone("alpha", "gamma").test("alpha-beta"));
        assertTrue(Strings.containsNoneIgnoreCase("GAMMA", "DELTA").test("alpha-beta"));
        assertFalse(Strings.containsNoneIgnoreCase("ALPHA", "gamma").test("alpha-beta"));

        assertTrue(Strings.containsAny("gamma", "beta").test("alpha-beta"));
        assertFalse(Strings.containsAny("gamma", "delta").test("alpha-beta"));
        assertTrue(Strings.containsAnyIgnoreCase("GAMMA", "BETA").test("alpha-beta"));
        assertFalse(Strings.containsAnyIgnoreCase("GAMMA", "DELTA").test("alpha-beta"));

        assertTrue(Strings.containsOne("alpha", "gamma").test("alpha-beta"));
        assertFalse(Strings.containsOne("alpha", "beta").test("alpha-beta"));
        assertTrue(Strings.containsOneIgnoreCase("ALPHA", "gamma").test("alpha-beta"));
        assertFalse(Strings.containsOneIgnoreCase("ALPHA", "BETA").test("alpha-beta"));
    }

    @Test
    public void containsStartsAndEndsPredicatesSupportBothCaseModes() {
        assertTrue(Strings.contains("middle").test("start-middle-end"));
        assertFalse(Strings.contains("MIDDLE").test("start-middle-end"));
        assertTrue(Strings.containsIgnoreCase("MIDDLE").test("start-middle-end"));

        assertTrue(Strings.startsWith("start").test("start-middle-end"));
        assertFalse(Strings.startsWith("START").test("start-middle-end"));
        assertTrue(Strings.startsWithIgnoreCase("START").test("start-middle-end"));

        assertTrue(Strings.endsWith("end").test("start-middle-end"));
        assertFalse(Strings.endsWith("END").test("start-middle-end"));
        assertTrue(Strings.endsWithIgnoreCase("END").test("start-middle-end"));
    }

    @Test
    public void caseAndLengthPredicatesExerciseBoundaryValues() {
        assertTrue(Strings.isUpper("ABC"));
        assertFalse(Strings.isUpper("AbC"));
        assertTrue(Strings.isLower("abc"));
        assertFalse(Strings.isLower("aBc"));
        assertTrue(Strings.isMixed("aB"));
        assertFalse(Strings.isMixed("ABC"));
        assertFalse(Strings.isMixed("123"));

        assertTrue(Strings.isLengthOf(length -> length == 4).test("test"));
        assertTrue(Strings.isLengthEq(4).test("test"));
        assertTrue(Strings.isLengthGe(4).test("tests"));
        assertFalse(Strings.isLengthGe(4).test("tes"));
        assertTrue(Strings.isLengthGt(4).test("tests"));
        assertFalse(Strings.isLengthGt(4).test("test"));
        assertTrue(Strings.isLengthLe(4).test("tes"));
        assertFalse(Strings.isLengthLe(4).test("tests"));
        assertTrue(Strings.isLengthLt(4).test("tes"));
        assertFalse(Strings.isLengthLt(4).test("test"));
    }

    @Test
    public void patternAndFormatPredicatesAcceptAndRejectRepresentativeValues() {
        assertTrue(Strings.matches("[a-z]+\\d+").test("test12"));
        assertFalse(Strings.matches("[a-z]+\\d+").test("test"));
        assertTrue(Strings.matches(Pattern.compile("[A-Z]{2}")).test("AB"));
        assertFalse(Strings.matches(Pattern.compile("[A-Z]{2}")).test("Ab"));

        assertTrue(Strings.isEmail("person@example.org"));
        assertFalse(Strings.isEmail("person-at-example.org"));
        assertTrue(Strings.isEmail().test("person@example.org"));
        assertFalse(Strings.isEmail().test("invalid"));

        assertTrue(Strings.isIPv4("192.168.1.10"));
        assertFalse(Strings.isIPv4("256.168.1.10"));
        assertTrue(Strings.isIPv4().test("127.0.0.1"));
        assertFalse(Strings.isIPv4().test("127.0.0"));
    }

    @Test
    public void blankAndCharacterClassifiersExerciseStaticAndPredicateForms() {
        assertTrue(Strings.isBlank(null));
        assertTrue(Strings.isBlank("  \t"));
        assertFalse(Strings.isBlank("value"));
        assertTrue(Strings.isBlank().test(" "));
        assertTrue(Strings.notBlank("value"));
        assertFalse(Strings.notBlank(null));
        assertTrue(Strings.notBlank().test("value"));

        assertTrue(Strings.isAlphanumeric("abc123"));
        assertFalse(Strings.isAlphanumeric("abc-123"));
        assertTrue(Strings.isAlphanumeric().test("abc123"));
        assertTrue(Strings.isNumeric("0123"));
        assertFalse(Strings.isNumeric("12a"));
        assertTrue(Strings.isNumeric().test("0123"));
        assertTrue(Strings.isAlphabetic("Привет"));
        assertFalse(Strings.isAlphabetic("abc1"));
        assertTrue(Strings.isAlphabetic().test("hello"));
        assertTrue(Strings.isUnicode("hello €"));
        assertFalse(Strings.isUnicode("\u0378"));
        assertTrue(Strings.isUnicode().test("текст"));
    }

    @Test
    public void factoriesRejectNullConfiguration() {
        assertThrows(NullPointerException.class, () -> Strings.in(null));
        assertThrows(NullPointerException.class, () -> Strings.containsAll((String[]) null));
        assertThrows(NullPointerException.class, () -> Strings.containsAny("valid", null));
        assertThrows(NullPointerException.class, () -> Strings.containsIgnoreCase(null));
        assertThrows(NullPointerException.class, () -> Strings.startsWith(null));
        assertThrows(NullPointerException.class, () -> Strings.endsWithIgnoreCase(null));
        assertThrows(NullPointerException.class, () -> Strings.isLengthOf(null));
        assertThrows(NullPointerException.class, () -> Strings.matches((String) null));
        assertThrows(NullPointerException.class, () -> Strings.matches((Pattern) null));
    }
}

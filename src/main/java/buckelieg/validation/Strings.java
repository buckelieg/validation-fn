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

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static buckelieg.validation.Utils.*;
import static java.util.Objects.requireNonNull;

/**
 * A collection of string-related predicates
 */
public final class Strings {

    private Strings() {
        throw new UnsupportedOperationException("No instances of Strings");
    }


    /**
     * Checks if provided value is an enumeration value
     *
     * @param enumeration an {@linkplain Enum}eration with checked values
     * @param <T>         enumeration type
     * @return true - if provided value is one of the elements of provided enum type<br/>false - otherwise
     * @throws NullPointerException if argument is null
     * @see Enum#valueOf(Class, String)
     */
    public static <T extends Enum<T>> Predicate<String> in(Class<T> enumeration) {
        requireNonNull(enumeration, "Enumeration type must be provided");
        return value -> {
            try {
                Enum.valueOf(enumeration, value);
            } catch (NullPointerException | IllegalArgumentException e) {
                return false;
            }
            return true;
        };
    }

    /**
     * Returns a {@linkplain Predicate} that checks if validated value contains ALL of provided <code>values</code> as substrings<br/>
     * This is case-sensitive predicate
     *
     * @param values a collection of values to be tested on
     * @return a {@linkplain Predicate} instance
     * @see String#contains(CharSequence)
     */
    public static Predicate<String> containsAll(String... values) {
        return value -> Stream.of(values).allMatch(value::contains);
    }

    /**
     * Returns a {@linkplain Predicate} that checks if validated value contains ALL of provided <code>values</code> as substrings ignoring case<br/>
     * This is case-insensitive predicate
     *
     * @param values a collection of values to be tested on
     * @return a {@linkplain Predicate} instance
     * @see String#contains(CharSequence)
     */
    public static Predicate<String> containsAllIgnoreCase(String... values) {
        return value -> {
            String lowerValue = value.toLowerCase();
            return Stream.of(values).map(String::toLowerCase).allMatch(lowerValue::contains);
        };
    }

    /**
     * Returns a {@linkplain Predicate} that checks if validated value contains NONE of provided <code>values</code> as substrings<br/>
     * This is case-sensitive predicate
     *
     * @param values a collection of values to be tested on
     * @return a {@linkplain Predicate} instance
     * @see String#contains(CharSequence)
     */
    public static Predicate<String> containsNone(String... values) {
        return value -> Stream.of(values).noneMatch(value::contains);
    }

    /**
     * Returns a {@linkplain Predicate} that checks if validated value contains NONE of provided <code>values</code> as substrings ignoring case<br/>
     * This is case-insensitive predicate
     *
     * @param values a collection of values to be tested on
     * @return a {@linkplain Predicate} instance
     * @see String#contains(CharSequence)
     */
    public static Predicate<String> containsNoneIgnoreCase(String... values) {
        return value -> {
            String lowerValue = value.toLowerCase();
            return Stream.of(values).map(String::toLowerCase).noneMatch(lowerValue::contains);
        };
    }

    /**
     * Returns a {@linkplain Predicate} that checks if validated value contains AT LEAST ONE of provided <code>values</code> as its substring<br/>
     * This is case-sensitive predicate
     *
     * @param values a collection of values to be tested on
     * @return a {@linkplain Predicate} instance
     * @see String#contains(CharSequence)
     */
    public static Predicate<String> containsAny(String... values) {
        return value -> Stream.of(values).anyMatch(value::contains);
    }

    /**
     * Returns a {@linkplain Predicate} that checks if validated value contains AT LEAST ONE of provided <code>values</code> as its substring ignoring case<br/>
     * This is case-insensitive predicate
     *
     * @param values a collection of values to be tested on
     * @return a {@linkplain Predicate} instance
     * @see String#contains(CharSequence)
     */
    public static Predicate<String> containsAnyIgnoreCase(String... values) {
        return value -> {
            String lowerValue = value.toLowerCase();
            return Stream.of(values).map(String::toLowerCase).anyMatch(lowerValue::contains);
        };
    }

    /**
     * Returns a {@linkplain Predicate} that checks if validated value contains STRICTLY ONE of provided <code>values</code> as its substring<br/>
     * This is case-sensitive predicate
     *
     * @param values a collection of values to be tested on
     * @return a {@linkplain Predicate} instance
     * @see String#contains(CharSequence)
     */
    public static Predicate<String> containsOne(String... values) {
        return value -> Stream.of(values).filter(value::contains).count() == 1;
    }

    /**
     * Returns a {@linkplain Predicate} that checks if validated value contains STRICTLY ONE of provided <code>values</code> as its substring ignoring case<br/>
     * This is case-insensitive predicate
     *
     * @param values a collection of values to be tested on
     * @return a {@linkplain Predicate} instance
     * @see String#contains(CharSequence)
     */
    public static Predicate<String> containsOneIgnoreCase(String... values) {
        return value -> {
            String lowerValue = value.toLowerCase();
            return Stream.of(values).map(String::toLowerCase).filter(lowerValue::contains).count() == 1;
        };
    }

    /**
     * Tests whether value contains provided part
     *
     * @param part a part to test value is contained in
     * @return a {@linkplain Predicate} instance
     * @see String#contains(CharSequence)
     */
    public static Predicate<String> contains(String part) {
        return value -> value.contains(part);
    }

    /**
     * Tests whether value contains provided part ignoring case
     *
     * @param part a part to test value is contained in
     * @return a {@linkplain Predicate} instance
     * @see String#contains(CharSequence)
     */
    public static Predicate<String> containsIgnoreCase(String part) {
        return value -> value.toLowerCase().contains(part.toLowerCase());
    }

    /**
     * Returns a {@linkplain Predicate} that checks if validated value ENDS WITH provided <code>ending</code> string<br/>
     * This is case-sensitive predicate
     *
     * @param ending a string value to te if this value is ended with
     * @return a {@linkplain Predicate} instance
     * @see String#endsWith(String)
     */
    public static Predicate<String> endsWith(String ending) {
        return value -> value.endsWith(ending);
    }

    /**
     * Returns a {@linkplain Predicate} that checks if validated value ENDS WITH provided <code>ending</code> string ignoring case<br/>
     * This is case-insensitive predicate
     *
     * @param ending a string value to te if this value is ended with
     * @return a {@linkplain Predicate} instance
     * @see String#endsWith(String)
     */
    public static Predicate<String> endsWithIgnoreCase(String ending) {
        return value -> value.toLowerCase().endsWith(ending.toLowerCase());
    }

    /**
     * Returns a {@linkplain Predicate} that checks if validated value STARTS WITH provided <code>starting</code> string<br/>
     * This is case-sensitive predicate
     *
     * @param starting a string value to te if this value is started with
     * @return a {@linkplain Predicate} instance
     * @see String#startsWith(String)
     */
    public static Predicate<String> startsWith(String starting) {
        return value -> value.startsWith(starting);
    }

    /**
     * Returns a {@linkplain Predicate} that checks if validated value STARTS WITH provided <code>starting</code> string ignoring case<br/>
     * This is case-insensitive predicate
     *
     * @param starting a string value to te if this value is started with
     * @return a {@linkplain Predicate} instance
     * @see String#startsWith(String)
     */
    public static Predicate<String> startsWithIgnoreCase(String starting) {
        return value -> value.toLowerCase().startsWith(starting.toLowerCase());
    }

    /**
     * Checks if provided value consist only of an UPPER cased characters
     *
     * @param value a validated value
     * @return true - if provided value is an UPPER cased string<br/>false - otherwise
     */
    public static boolean isUpper(String value) {
        return value.equals(value.toUpperCase());
    }

    /**
     * Checks if provided value consist only of an LOWER cased characters
     *
     * @param value a validated value
     * @return true - if provided value is an LOWER cased string<br/>false - otherwise
     */
    public static boolean isLower(String value) {
        return value.equals(value.toLowerCase());
    }

    /**
     * Returns a {@linkplain Predicate} that checks if length of value conforms provided predicate
     *
     * @param predicate a predicate to check this string length against
     * @return a {@linkplain Predicate} instance
     * @throws NullPointerException if <code>predicate</code> is null
     * @see Predicates#ge(Comparable)
     */
    public static Predicate<String> isLengthOf(Predicate<Integer> predicate) {
        requireNonNull(predicate, "Predicate must be provided");
        return isMeasuredAt(String::length, predicate);
    }

    /**
     * Returns a {@linkplain Predicate} that checks if length of value is GREATER THAN provided <code>measure</code>
     *
     * @param measure maximum length value
     * @return a {@linkplain Predicate} instance
     * @see Predicates#ge(Comparable)
     */
    public static Predicate<String> isLengthGe(int measure) {
        return isLengthOf(Predicates.ge(measure));
    }

    /**
     * Returns a {@linkplain Predicate} that checks if length of value is GREATER THAN OR EQUAL TO provided <code>measure</code>
     *
     * @param measure maximum length value
     * @return a {@linkplain Predicate} instance
     * @see Predicates#gt(Comparable)
     */
    public static Predicate<String> isLengthGt(int measure) {
        return isLengthOf(Predicates.gt(measure));
    }

    /**
     * Returns a {@linkplain Predicate} that checks if length of value is LESS THAN OR EQUAL TO provided <code>measure</code>
     *
     * @param measure minimum length value
     * @return a {@linkplain Predicate} instance
     * @see Predicates#le(Comparable)
     */
    public static Predicate<String> isLengthLe(int measure) {
        return isLengthOf(Predicates.le(measure));
    }

    /**
     * Returns a {@linkplain Predicate} that checks if length of value is LESS THAN provided <code>measure</code>
     *
     * @param measure minimum length value
     * @return a {@linkplain Predicate} instance
     * @see Predicates#lt(Comparable)
     */
    public static Predicate<String> isLengthLt(int measure) {
        return isLengthOf(Predicates.lt(measure));
    }

    /**
     * Returns a {@linkplain Predicate} that checks if length of value is EQUAL TO provided <code>measure</code>
     *
     * @param measure minimum length value
     * @return a {@linkplain Predicate} instance
     * @see Predicates#eq(Comparable)
     */
    public static Predicate<String> isLengthEq(int measure) {
        return isLengthOf(Predicates.eq(measure));
    }

    /**
     * Returns a {@linkplain Predicate} that checks if provided value matches specific regular expression pattern
     *
     * @param pattern a regular expression
     * @return a {@linkplain Predicate} instance
     * @see String#matches(String)
     */
    public static Predicate<String> matches(String pattern) {
        return value -> value.matches(pattern);
    }

    /**
     * Returns a {@linkplain Predicate} that checks if provided value matches specific regular expression pattern
     *
     * @param pattern a regular expression pattern
     * @return a {@linkplain Predicate} instance
     * @throws NullPointerException if <code>pattern</code> is null
     * @see Pattern#matcher(CharSequence)
     * @see Matcher#matches()
     */
    public static Predicate<String> matches(Pattern pattern) {
        requireNonNull(pattern, "Pattern must be provided");
        return value -> pattern.matcher(value).matches();
    }

    /**
     * Validates provided value to be an <code>e-mail</code>-formatted string:<br/>
     * <pre>{@code
     * "^[\\w-+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-zA-Z]{2,})$"
     * }</pre>
     *
     * @param value a validated value
     * @return true - if provided value matches an <code>e-mail</code> string<br/>false - otherwise
     */
    public static boolean isEmail(String value) {
        return PATTERN_EMAIL.matcher(value).matches();
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Strings#isEmail} method
     *
     * @return a {@linkplain Predicate} instance
     * @see #isEmail(String)
     */
    public static Predicate<String> isEmail() {
        return Strings::isEmail;
    }

    /**
     * Checks provided string value to conform next pattern:
     * <pre>{@code
     * "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))"
     * }</pre>
     *
     * @param value a validated value
     * @return true - if this string conforms the IPv$ address fromat<br/>false - otherwise
     */
    public static boolean isIPv4(String value) {
        return PATTERN_IPv4_ADDRESS.matcher(value).matches();
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Strings#isIPv4(String)} method
     *
     * @return a {@linkplain Predicate} instance
     * @see #isIPv4(String)
     */
    public static Predicate<String> isIPv4() {
        return Strings::isIPv4;
    }

    /**
     * Pre-Java 11 implementation<br/>
     * For Java 11 it is recommended to use a predicate from already available functionality:
     * <pre>{@code
     * var predicate = Predicates.<String>of(Objects::isNull).or(String::isBlank);
     * }</pre>
     *
     * @param value a validated value
     * @return true - if provided value is <code>null</code> OR length of the trimmed value is <code>0</code><br/>false - otherwise
     * @see String#trim()
     * @see String#isEmpty()
     */
    public static boolean isBlank(String value) {
        return null == value || value.trim().isEmpty();
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Strings#isBlank(String)} method
     *
     * @return a {@linkplain Predicate} instance
     */
    public static Predicate<String> isBlank() {
        return Strings::isBlank;
    }

    /**
     * Checks if provided string value is not blank
     *
     * @param value a validated value
     * @return true - if provided value is not <code>null</code> AND length of the trimmed value is greater than <code>0</code><br/>false - otherwise
     * @see #isBlank(String)
     */
    public static boolean notBlank(String value) {
        return !isBlank(value);
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for negated result of {@linkplain Strings#isBlank(String)} method
     *
     * @return a {@linkplain Predicate} instance
     */
    public static Predicate<String> notBlank() {
        return Strings::notBlank;
    }

    /**
     * Checks if provided string consists only of numbers and characters
     *
     * @param value a validated value
     * @return true - if provided string is an alphanumeric string<br/>false - otherwise
     * @see Character#isLetterOrDigit(char)
     * @see Character#isLetterOrDigit(int)
     */
    public static boolean isAlphanumeric(String value) {
        return allCharactersMatch(value, Character::isLetterOrDigit);
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Strings#isAlphanumeric(String)} method
     *
     * @return a {@linkplain Predicate} instance
     * @see Strings#isAlphanumeric(String)
     */
    public static Predicate<String> isAlphanumeric() {
        return Strings::isAlphanumeric;
    }

    /**
     * Checks if provided string consists only of numbers
     *
     * @param value a validated value
     * @return true - if provided string is a numeric string<br/>false - otherwise
     * @see Character#isDigit(char)
     * @see Character#isDigit(int)
     */
    public static boolean isNumeric(String value) {
        return allCharactersMatch(value, Character::isDigit);
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Strings#isNumeric(String)} method
     *
     * @return a {@linkplain Predicate} instance
     * @see Strings#isNumeric(String)
     */
    public static Predicate<String> isNumeric() {
        return Strings::isNumeric;
    }

    /**
     * Checks if provided string consists only of numbers
     *
     * @param value a validated value
     * @return true - if provided string is a numeric string<br/>false - otherwise
     * @see Character#isDefined(char)
     * @see Character#isDefined(int)
     */
    public static boolean isUnicode(String value) {
        return allCharactersMatch(value, Character::isDefined);
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Strings#isNumeric(String)} method
     *
     * @return a {@linkplain Predicate} instance
     * @see Strings#isUnicode(String)
     */
    public static Predicate<String> isUnicode() {
        return Strings::isUnicode;
    }

    /**
     * Checks if provided string consists only of alphabetic symbols
     *
     * @param value a validated value
     * @return true - if provided string is a alphabetic string<br/>false - otherwise
     * @see Character#isAlphabetic(int)
     */
    public static boolean isAlphabetic(String value) {
        return allCharactersMatch(value, Character::isAlphabetic);
    }

    /**
     * Returns a {@linkplain Predicate} wrapper for {@linkplain Strings#isAlphabetic(String)} method
     *
     * @return a {@linkplain Predicate} instance
     * @see Strings#isUnicode(String)
     */
    public static Predicate<String> isAlphabetic() {
        return Strings::isAlphabetic;
    }
}

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
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A collection of string-related predicates
 */
public final class Strings {

    private Strings() {
        throw new UnsupportedOperationException("No instances of Strings");
    }

    /**
     * Taken from here: https://stackoverflow.com/a/47181151
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-zA-Z]{2,})$");

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
     * Returns a {@linkplain Predicate} that checks if validated value STARTS WITH provided <code>starting</code> string<br/>
     * This is case-sensitive predicate
     *
     * @param starting a string value to te if this value is started with
     * @return a {@linkplain Predicate} instance
     * @see String#startsWith(String)
     */
    public static Predicate<String> startWith(String starting) {
        return value -> value.startsWith(starting);
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
     * Returns a {@linkplain Predicate} that checks maximum length of provided value
     *
     * @param maxLength maximum length value
     * @return a {@linkplain Predicate} instance
     */
    public static Predicate<String> maxLength(long maxLength) {
        return value -> value.length() >= maxLength;
    }

    /**
     * Returns a {@linkplain Predicate} that checks minimum length of provided value
     *
     * @param minLength minimum length value
     * @return a {@linkplain Predicate} instance
     */
    public static Predicate<String> minLength(long minLength) {
        return value -> value.length() <= minLength;
    }

    /**
     * Returns a {@linkplain Predicate} that checks if provided value mathes specific regular expression pattern
     *
     * @param pattern a regular expression
     * @return a {@linkplain Predicate} instance
     * @see String#matches(String)
     */
    public static Predicate<String> match(String pattern) {
        return value -> value.matches(pattern);
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
        return EMAIL_PATTERN.matcher(value).matches();
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

}

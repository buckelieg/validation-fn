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

import buckelieg.validation.ValidationException;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * An interface of validation chain<br/>
 * Typical usage listed below:
 * <pre>{@code
 * // Suppose you have a class listed below:
 *
 * public class MyClass {
 *     private String stringProperty;
 *     public MyClass(String stringProperty) {
 *         this.stringProperty = stringProperty;
 *     }
 *     public MyClass() {
 *         this(null);
 *     }
 *     public String getStringProperty {
 *         return stringProperty;
 *     }
 * }
 *
 * // and it is needed some validation to be performed upon it...
 *
 * var validator = Validator.<MyClass>notNull("MyClass instance must be provided")
 *                          .thenMap(
 *                              MyClass::getStringProperty,
 *                              Predicates.<String>of(Objects::isNull).or(String::isBlank),
 *                              "stringProperty must not be null nor blank"
 *                          );
 *
 *  // case 1:
 *  validator.validate(new MyClass());
 *  // will throw a ValidationException with user message of "stringProperty must not be null nor blank"
 *
 *  // case2:
 *  validator.validate(null);
 *  // will throw a ValidationException with user message of "MyClass instance must be provided"
 *
 *  // Optional checks:
 *  var validator = Validator.<MyClass>empty()
 *                           .thenMapIfNotNull(
 *                              MyClass::getStringProperty,
 *                              Predicates.<String>of(Objects::isNull).or(String::isBlank),
 *                              "stringProperty must not be null nor blank"
 *                           );
 *  validator.validate(null);
 *  // this case will throw nothing since we passed null as an argument to validation function
 * }</pre>
 *
 * @param <T> validated value type
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Constructs a <code>Validator</code> instance from provided lambda function
     *
     * @param validator a lambda function to be returned as an instance
     * @param <T>       validated value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    static <T> Validator<T> of(Validator<T> validator) {
        return requireNonNull(validator, "Validator must be provided");
    }

    /**
     * Constructs a {@linkplain Validator} instance from provided predicate and error message supplier function
     * The validator logic will be: whether predicate evaluates to <code>true</code> then {@linkplain ValidationException} will be thrown with message provided by <code>messageSupplier</code> function:<br/>
     * <pre>{@code
     * value -> {
     *      if (predicate.test(value)) {
     *          throw new ValidationException(messageSupplier.apply(value));
     *     }
     * }
     * }</pre>
     *
     * @param predicate       validation test case
     * @param messageSupplier an error message supplier function
     * @param <T>             validated value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    static <T> Validator<T> ofPredicate(Predicate<T> predicate, Function<T, String> messageSupplier) {
        requireNonNull(predicate, "Predicate predicate must be provided");
        requireNonNull(messageSupplier, "Message supplier must be provided");
        return value -> {
            if (predicate.test(value)) {
                throw new ValidationException(messageSupplier.apply(value));
            }
        };
    }

    /**
     * Constructs a {@linkplain Validator} instance from provided predicate and provided error message
     *
     * @param predicate    validation test case
     * @param errorMessage an error message
     * @param <T>          validated value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     * @see Validator#ofPredicate(Predicate, Function)
     */
    static <T> Validator<T> ofPredicate(Predicate<T> predicate, String errorMessage) {
        return ofPredicate(predicate, value -> errorMessage);
    }

    /**
     * Return an empty validator that does nothing. This is intended to be a start of any validation chain
     *
     * @param <T> validated value type
     * @return a <code>Validator</code> instance
     */
    static <T> Validator<T> empty() {
        return value -> {
        };
    }

    /**
     * Constructs a {@linkplain Validator} instance that tests value for not being null with error message supplier function
     *
     * @param messageSupplier an error message supplier function
     * @param <T>             validated value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    static <T> Validator<T> notNull(Supplier<String> messageSupplier) {
        return ofPredicate(Objects::isNull, requireNonNull(messageSupplier, "Error message supplier must be provided").get());
    }

    /**
     * Constructs a {@linkplain Validator} instance that tests value for not being null with provided error message
     *
     * @param errorMessage an error message
     * @param <T>          validated value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    static <T> Validator<T> notNull(String errorMessage) {
        return notNull(() -> errorMessage);
    }

    /**
     * Constructs a {@linkplain Validator} instance that tests value for nullness with default error message of: <code>Provided value must not be null</code>
     *
     * @param <T> validated value type
     * @return a <code>Validator</code> instance
     */
    static <T> Validator<T> notNull() {
        return notNull("Provided value must not be null");
    }

    /**
     * Constructs a {@linkplain Validator} instance that tests value for nullness with error message supplier function
     *
     * @param messageSupplier an error message supplier function
     * @param <T>             validated value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    static <T> Validator<T> isNull(Supplier<String> messageSupplier) {
        return ofPredicate(Objects::nonNull, requireNonNull(messageSupplier, "Error message supplier must be provided").get());
    }

    /**
     * Constructs a {@linkplain Validator} instance that tests value for nullness with provided error message
     *
     * @param errorMessage an error message
     * @param <T>          validated value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    static <T> Validator<T> isNull(String errorMessage) {
        return isNull(() -> errorMessage);
    }

    /**
     * Constructs a {@linkplain Validator} instance that tests value for nullness with default error message of: <code>Provided value must be null</code>
     *
     * @param <T> validated value type
     * @return a <code>Validator</code> instance
     */
    static <T> Validator<T> isNull() {
        return isNull("Provided value must be null");
    }

    /**
     * Validates provided value possibly throwing a {@linkplain ValidationException}
     *
     * @param value a validated value
     * @throws ValidationException in case of validation login fails
     */
    void validate(T value) throws ValidationException;

    /**
     * Collects an error as an {@link Optional}<br/>
     * If validation fails - then optional is returned filled with a <code>ValidationException</code><br/>
     * Otherwise - empty optional is returned
     *
     * @param value a validated value
     * @return an empty optional if there is no validation errors, otherwise - non-empty optional with an error
     */
    default Optional<ValidationException> collect(T value) {
        try {
            validate(value);
        } catch (ValidationException e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }

    /**
     * Converts this validator to a {@link Predicate}<br/>
     * If validation fails (i.e. validation exception is thrown) - then returned predicates turns to <code>true</code><br/>
     * Otherwise - predicate returns <code>false</code>
     *
     * @return a predicate based on this validator
     */
    default Predicate<T> toPredicate() {
        return value -> {
            try {
                validate(value);
                return false;
            } catch (ValidationException e) {
                return true;
            }
        };
    }

    /**
     * Composes a new <code>Validator</code> with a next validation step which will be executed whenever provided condition {@linkplain Predicate} evaluates to <code>true</code>
     *
     * @param condition a condition to be met to proceed with the next validation step
     * @param next      a next step {@linkplain Validator}
     * @return a new composite <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    default Validator<T> thenIf(Predicate<T> condition, Validator<T> next) {
        requireNonNull(condition, "Condition predicate must be provided");
        requireNonNull(next, "Validator must be provided");
        return value -> {
            validate(value);
            if (condition.test(value)) {
                next.validate(value);
            }
        };
    }

    /**
     * Composes a new <code>Validator</code> with a next validation step which will always be executed
     *
     * @param next a next step {@linkplain Validator}
     * @return a new composite <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    default Validator<T> then(Validator<T> next) {
        return thenIf(value -> true, next);
    }

    /**
     * Composes a new <code>Validator</code> with a next validation step constructed from provided {@linkplain Predicate} and error message supplier {@linkplain Function} which will always be executed
     *
     * @param predicate       validation test case
     * @param messageSupplier an error message supplier function
     * @return a new composite <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     * @see Validator#ofPredicate(Predicate, Function)
     */
    default Validator<T> then(Predicate<T> predicate, Function<T, String> messageSupplier) {
        return then(ofPredicate(predicate, messageSupplier));
    }

    /**
     * Composes a new <code>Validator</code> with a next validation step constructed from provided {@linkplain Predicate} and error message which will always be executed
     *
     * @param predicate    validation test case
     * @param errorMessage an error message
     * @return a new composite <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     * @see Validator#ofPredicate(Predicate, String)
     */
    default Validator<T> then(Predicate<T> predicate, String errorMessage) {
        return then(predicate, value -> errorMessage);
    }

    /**
     * Composes a new <code>Validator</code> with a next validation step which will be executed whenever validated object is not <code>null</code>
     *
     * @param next a next step {@linkplain Validator}
     * @return a new composite <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    default Validator<T> thenIfNotNull(Validator<T> next) {
        return thenIf(Objects::nonNull, next);
    }

    /**
     * Composes a new <code>Validator</code> with a next validation step constructed from provided {@linkplain Predicate} and error message supplier {@linkplain Function} which will be executed whenever validated object is not <code>null</code>
     *
     * @param predicate       validation test case
     * @param messageSupplier an error message supplier function
     * @return a new composite <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     * @see Validator#ofPredicate(Predicate, Function)
     */
    default Validator<T> thenIfNotNull(Predicate<T> predicate, Function<T, String> messageSupplier) {
        return thenIfNotNull(ofPredicate(predicate, messageSupplier));
    }

    /**
     * Composes a new <code>Validator</code> with a next validation step constructed from provided {@linkplain Predicate} and error message which will be executed whenever validated object is not <code>null</code>
     *
     * @param predicate    validation test case
     * @param errorMessage an error message
     * @return a new composite <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     * @see Validator#ofPredicate(Predicate, String)
     */
    default Validator<T> thenIfNotNull(Predicate<T> predicate, String errorMessage) {
        return thenIfNotNull(predicate, value -> errorMessage);
    }

    /**
     * Composes a new <code>Validator</code> which next validation step (that will be conditionally executed) will operate on the mapped value
     *
     * @param condition   a condition to be met to proceed with the next validation step
     * @param valueMapper validated value mapper
     * @param next        a next step {@linkplain Validator}
     * @param <R>         validated mapped value type
     * @return a new composite <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapIf(Predicate<T> condition, Function<T, R> valueMapper, Validator<R> next) {
        requireNonNull(condition, "Condition predicate must be provided");
        requireNonNull(valueMapper, "Value mapping function must be provided");
        requireNonNull(next, "Mapped value validator must be provided");
        return thenIf(condition, value -> next.validate(valueMapper.apply(value)));
    }

    /**
     * Composes a new <code>Validator</code> which next validation step (that will be conditionally executed) will operate on the mapped value
     *
     * @param condition       a condition to be met to proceed with the next validation step
     * @param valueMapper     validated value mapper
     * @param predicate       validation test case
     * @param messageSupplier an error message supplier function
     * @param <R>             validated mapped value type
     * @return a new composite <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapIf(Predicate<T> condition, Function<T, R> valueMapper, Predicate<R> predicate, Function<R, String> messageSupplier) {
        return thenMapIf(condition, valueMapper, Validator.ofPredicate(predicate, messageSupplier));
    }

    /**
     * Composes a new <code>Validator</code> which next validation step (that will be conditionally executed) will operate on the mapped value
     *
     * @param condition    a condition to be met to proceed with the next validation step
     * @param valueMapper  validated value mapper
     * @param predicate    validation test case
     * @param errorMessage an error message
     * @param <R>          validated mapped value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapIf(Predicate<T> condition, Function<T, R> valueMapper, Predicate<R> predicate, String errorMessage) {
        return thenMapIf(condition, valueMapper, predicate, value -> errorMessage);
    }

    /**
     * Composes a new <code>Validator</code> which next validation step (that will always be executed) will operate on the mapped value
     *
     * @param valueMapper validated value mapper
     * @param next        a next step {@linkplain Validator}
     * @param <R>         validated mapped value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMap(Function<T, R> valueMapper, Validator<R> next) {
        return thenMapIf(value -> true, valueMapper, next);
    }

    /**
     * Composes a new <code>Validator</code> which next validation step (that will always be executed) will operate on the mapped value
     *
     * @param valueMapper     validated value mapper
     * @param predicate       validation test case
     * @param messageSupplier an error message supplier function
     * @param <R>             validated mapped value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMap(Function<T, R> valueMapper, Predicate<R> predicate, Function<R, String> messageSupplier) {
        return thenMap(valueMapper, ofPredicate(predicate, messageSupplier));
    }

    /**
     * Composes a new <code>Validator</code> which next validation step (that will always be executed) will operate on the mapped value
     *
     * @param valueMapper  validated value mapper
     * @param predicate    validation test case
     * @param errorMessage an error message
     * @param <R>          validated mapped value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMap(Function<T, R> valueMapper, Predicate<R> predicate, String errorMessage) {
        return thenMap(valueMapper, predicate, value -> errorMessage);
    }

    /**
     * Composes a new <code>Validator</code> which next validation step (that will be executed if validated object is not <code>null</code>) will operate on the mapped value
     *
     * @param valueMapper validated value mapper
     * @param next        a next step {@linkplain Validator}
     * @param <R>         validated mapped value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapIfNotNull(Function<T, R> valueMapper, Validator<R> next) {
        return thenMapIf(Objects::nonNull, valueMapper, next);
    }

    /**
     * Composes a new <code>Validator</code> which next validation step (that will be executed if validated object is not <code>null</code>) will operate on the mapped value
     *
     * @param valueMapper     validated value mapper
     * @param predicate       validation test case
     * @param messageSupplier an error message supplier function
     * @param <R>             validated mapped value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapIfNotNull(Function<T, R> valueMapper, Predicate<R> predicate, Function<R, String> messageSupplier) {
        return thenMapIfNotNull(valueMapper, ofPredicate(predicate, messageSupplier));
    }

    /**
     * Composes a new <code>Validator</code> which next validation step (that will be executed if validated object is not <code>null</code>) will operate on the mapped value
     *
     * @param valueMapper  validated value mapper
     * @param predicate    mapped value validation test case
     * @param errorMessage an error message
     * @param <R>          validated mapped value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapIfNotNull(Function<T, R> valueMapper, Predicate<R> predicate, String errorMessage) {
        return thenMapIfNotNull(valueMapper, predicate, value -> errorMessage);
    }
}
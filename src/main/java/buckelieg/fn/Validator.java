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
package buckelieg.fn;

import buckelieg.validation.ValidationException;
import buckelieg.validation.Validators;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;

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
 * var validator = Validators.<MyClass>notNull("MyClass instance must be provided")
 *                          .thenMapRequire(
 *                              MyClass::getStringProperty,
 *                              value -> value != null && !value.trim().isEmpty(),
 *                              "stringProperty must not be null nor blank"
 *                          );
 *
 *  // case 1:
 *  MyClass value = validator.validate(new MyClass());
 *  // will throw a ValidationException with user message of "stringProperty must not be null nor blank"
 *
 *  // case2:
 *  MyClass value = validator.validate(null);
 *  // will throw a ValidationException with user message of "MyClass instance must be provided"
 *
 *  // Optional checks:
 *  var validator = Validator.<MyClass>of()
 *                           .thenMapIfNotNull(
 *                              MyClass::getStringProperty,
 *                              Validator.require(
 *                                  value -> value != null && !value.trim().isEmpty(),
 *                                  "stringProperty must not be null nor blank"
 *                              )
 *                           );
 *  MyClass value = validator.validate(null);
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
     * Return an empty validator that does nothing. This is intended to be a start of any validation chain
     *
     * @param <T> validated value type
     * @return a <code>Validator</code> instance
     */
    static <T> Validator<T> of() {
        return of(value -> value);
    }

    /**
     * Builds a new validator instance from empty one
     *
     * @param builder validator instance builder
     * @param <T>     validated element type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument or provided validator is null
     * @see UnaryOperator
     * @see #of()
     */
    static <T> Validator<T> build(UnaryOperator<Validator<T>> builder) {
        return requireNonNull(requireNonNull(builder, "Validator builder function must be provided").apply(of()), "Validator instance must be provided");
    }

    /**
     * Constructs a validator that rejects values matching the provided predicate.
     *
     * @param predicate       predicate describing an invalid value
     * @param messageSupplier validation failure message supplier
     * @param <T>             validated value type
     * @return a validator that fails when the predicate returns {@code true}
     * @throws NullPointerException if any argument is null
     */
    static <T> Validator<T> rejectIf(Predicate<? super T> predicate, Function<? super T, String> messageSupplier) {
        requireNonNull(predicate, "Predicate must be provided");
        requireNonNull(messageSupplier, "Message supplier must be provided");
        return value -> {
            if (predicate.test(value)) throw new ValidationException(messageSupplier.apply(value));
            return value;
        };
    }

    /**
     * Constructs a validator that rejects values matching the provided predicate.
     *
     * @param predicate    predicate describing an invalid value
     * @param errorMessage validation failure message
     * @param <T>          validated value type
     * @return a validator that fails when the predicate returns {@code true}
     * @throws NullPointerException if any argument is null
     */
    static <T> Validator<T> rejectIf(Predicate<? super T> predicate, String errorMessage) {
        requireNonNull(errorMessage, "Error message must be provided");
        return rejectIf(predicate, value -> errorMessage);
    }

    /**
     * Constructs a validator that requires values to match the provided predicate.
     *
     * @param predicate       predicate describing a valid value
     * @param messageSupplier validation failure message supplier
     * @param <T>             validated value type
     * @return a validator that fails when the predicate returns {@code false}
     * @throws NullPointerException if any argument is null
     */
    static <T> Validator<T> require(Predicate<? super T> predicate, Function<? super T, String> messageSupplier) {
        Predicate<? super T> valid = requireNonNull(predicate, "Predicate must be provided");
        return rejectIf(value -> !valid.test(value), messageSupplier);
    }

    /**
     * Constructs a validator that requires values to match the provided predicate.
     *
     * @param predicate    predicate describing a valid value
     * @param errorMessage validation failure message
     * @param <T>          validated value type
     * @return a validator that fails when the predicate returns {@code false}
     * @throws NullPointerException if any argument is null
     */
    static <T> Validator<T> require(Predicate<? super T> predicate, String errorMessage) {
        requireNonNull(errorMessage, "Error message must be provided");
        return require(predicate, value -> errorMessage);
    }

    /**
     * Validates provided value possibly throwing a {@linkplain ValidationException}
     *
     * @param value a validated value
     * @return a validated value
     * @throws ValidationException in case of validation login fails
     */
    T validate(T value) throws ValidationException;

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
     * Converts this validator to a predicate that returns {@code true} for valid values.
     *
     * @return a predicate that follows normal validity semantics
     */
    default Predicate<T> asValidPredicate() {
        return value -> {
            try {
                validate(value);
                return true;
            } catch (ValidationException e) {
                return false;
            }
        };
    }

    /**
     * Converts this validator to a predicate that returns {@code true} for invalid values.
     *
     * @return a predicate that describes validation failure
     */
    default Predicate<T> asInvalidPredicate() {
        return asValidPredicate().negate();
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
            if (condition.test(value)) next.validate(value);
            return value;
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
     * Appends a validation step that requires the predicate to return {@code true}.
     *
     * @param predicate       predicate describing a valid value
     * @param messageSupplier validation failure message supplier
     * @return a new composite validator
     */
    default Validator<T> thenRequire(Predicate<? super T> predicate, Function<? super T, String> messageSupplier) {
        return then(require(predicate, messageSupplier));
    }

    /**
     * Appends a validation step that requires the predicate to return {@code true}.
     *
     * @param predicate    predicate describing a valid value
     * @param errorMessage validation failure message
     * @return a new composite validator
     */
    default Validator<T> thenRequire(Predicate<? super T> predicate, String errorMessage) {
        return then(require(predicate, errorMessage));
    }

    /**
     * Appends a validation step that rejects the value when the predicate returns {@code true}.
     *
     * @param predicate       predicate describing an invalid value
     * @param messageSupplier validation failure message supplier
     * @return a new composite validator
     */
    default Validator<T> thenRejectIf(Predicate<? super T> predicate, Function<? super T, String> messageSupplier) {
        return then(rejectIf(predicate, messageSupplier));
    }

    /**
     * Appends a validation step that rejects the value when the predicate returns {@code true}.
     *
     * @param predicate    predicate describing an invalid value
     * @param errorMessage validation failure message
     * @return a new composite validator
     */
    default Validator<T> thenRejectIf(Predicate<? super T> predicate, String errorMessage) {
        return then(rejectIf(predicate, errorMessage));
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
        return thenIf(condition, Validators.map(valueMapper, next));
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
     * Maps the value and rejects it when the mapped-value predicate returns {@code true}.
     *
     * @param valueMapper     maps the original value
     * @param predicate       failure predicate receiving the mapped and original values
     * @param messageSupplier failure message supplier receiving the mapped and original values
     * @param <R>             mapped value type
     * @return a composite validator that returns the original value on success
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapRejectIf(Function<T, R> valueMapper, BiPredicate<R, T> predicate,
                                             BiFunction<R, T, String> messageSupplier) {
        return then(Validators.mapRejectIf(valueMapper, predicate, messageSupplier));
    }

    /**
     * Maps the value and rejects it when the mapped-value predicate returns {@code true}.
     *
     * @param valueMapper     maps the original value
     * @param predicate       failure predicate receiving the mapped and original values
     * @param messageSupplier failure message supplier receiving the mapped value
     * @param <R>             mapped value type
     * @return a composite validator that returns the original value on success
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapRejectIf(Function<T, R> valueMapper, BiPredicate<R, T> predicate,
                                             Function<R, String> messageSupplier) {
        return then(Validators.mapRejectIf(valueMapper, predicate, messageSupplier));
    }

    /**
     * Maps the value and rejects it when the mapped-value predicate returns {@code true}.
     *
     * @param valueMapper  maps the original value
     * @param predicate    failure predicate receiving the mapped and original values
     * @param errorMessage constant validation failure message
     * @param <R>          mapped value type
     * @return a composite validator that returns the original value on success
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapRejectIf(Function<T, R> valueMapper, BiPredicate<R, T> predicate,
                                             String errorMessage) {
        return then(Validators.mapRejectIf(valueMapper, predicate, errorMessage));
    }

    /**
     * Maps the value and rejects it when the mapped-value predicate returns {@code true}.
     *
     * @param valueMapper     maps the original value
     * @param predicate       failure predicate receiving the mapped value
     * @param messageSupplier failure message supplier receiving the mapped and original values
     * @param <R>             mapped value type
     * @return a composite validator that returns the original value on success
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapRejectIf(Function<T, R> valueMapper, Predicate<R> predicate,
                                             BiFunction<R, T, String> messageSupplier) {
        return then(Validators.mapRejectIf(valueMapper, predicate, messageSupplier));
    }

    /**
     * Maps the value and rejects it when the mapped-value predicate returns {@code true}.
     *
     * @param valueMapper     maps the original value
     * @param predicate       failure predicate receiving the mapped value
     * @param messageSupplier failure message supplier receiving the mapped value
     * @param <R>             mapped value type
     * @return a composite validator that returns the original value on success
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapRejectIf(Function<T, R> valueMapper, Predicate<R> predicate,
                                             Function<R, String> messageSupplier) {
        return then(Validators.mapRejectIf(valueMapper, predicate, messageSupplier));
    }

    /**
     * Maps the value and rejects it when the mapped-value predicate returns {@code true}.
     *
     * @param valueMapper  maps the original value
     * @param predicate    failure predicate receiving the mapped value
     * @param errorMessage constant validation failure message
     * @param <R>          mapped value type
     * @return a composite validator that returns the original value on success
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapRejectIf(Function<T, R> valueMapper, Predicate<R> predicate,
                                             String errorMessage) {
        return then(Validators.mapRejectIf(valueMapper, predicate, errorMessage));
    }

    /**
     * Maps the value and requires the mapped-value predicate to return {@code true}.
     *
     * @param valueMapper     maps the original value
     * @param predicate       validity predicate receiving the mapped and original values
     * @param messageSupplier failure message supplier receiving the mapped and original values
     * @param <R>             mapped value type
     * @return a composite validator that returns the original value on success
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapRequire(Function<T, R> valueMapper, BiPredicate<R, T> predicate,
                                            BiFunction<R, T, String> messageSupplier) {
        return then(Validators.mapRequire(valueMapper, predicate, messageSupplier));
    }

    /**
     * Maps the value and requires the mapped-value predicate to return {@code true}.
     *
     * @param valueMapper     maps the original value
     * @param predicate       validity predicate receiving the mapped and original values
     * @param messageSupplier failure message supplier receiving the mapped value
     * @param <R>             mapped value type
     * @return a composite validator that returns the original value on success
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapRequire(Function<T, R> valueMapper, BiPredicate<R, T> predicate,
                                            Function<R, String> messageSupplier) {
        return then(Validators.mapRequire(valueMapper, predicate, messageSupplier));
    }

    /**
     * Maps the value and requires the mapped-value predicate to return {@code true}.
     *
     * @param valueMapper  maps the original value
     * @param predicate    validity predicate receiving the mapped and original values
     * @param errorMessage constant validation failure message
     * @param <R>          mapped value type
     * @return a composite validator that returns the original value on success
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapRequire(Function<T, R> valueMapper, BiPredicate<R, T> predicate,
                                            String errorMessage) {
        return then(Validators.mapRequire(valueMapper, predicate, errorMessage));
    }

    /**
     * Maps the value and requires the mapped-value predicate to return {@code true}.
     *
     * @param valueMapper     maps the original value
     * @param predicate       validity predicate receiving the mapped value
     * @param messageSupplier failure message supplier receiving the mapped and original values
     * @param <R>             mapped value type
     * @return a composite validator that returns the original value on success
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapRequire(Function<T, R> valueMapper, Predicate<R> predicate,
                                            BiFunction<R, T, String> messageSupplier) {
        return then(Validators.mapRequire(valueMapper, predicate, messageSupplier));
    }

    /**
     * Maps the value and requires the mapped-value predicate to return {@code true}.
     *
     * @param valueMapper     maps the original value
     * @param predicate       validity predicate receiving the mapped value
     * @param messageSupplier failure message supplier receiving the mapped value
     * @param <R>             mapped value type
     * @return a composite validator that returns the original value on success
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapRequire(Function<T, R> valueMapper, Predicate<R> predicate,
                                            Function<R, String> messageSupplier) {
        return then(Validators.mapRequire(valueMapper, predicate, messageSupplier));
    }

    /**
     * Maps the value and requires the mapped-value predicate to return {@code true}.
     *
     * @param valueMapper  maps the original value
     * @param predicate    validity predicate receiving the mapped value
     * @param errorMessage constant validation failure message
     * @param <R>          mapped value type
     * @return a composite validator that returns the original value on success
     * @throws NullPointerException if any argument is null
     */
    default <R> Validator<T> thenMapRequire(Function<T, R> valueMapper, Predicate<R> predicate,
                                            String errorMessage) {
        return then(Validators.mapRequire(valueMapper, predicate, errorMessage));
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

}

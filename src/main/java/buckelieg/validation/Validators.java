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
package buckelieg.validation;

import buckelieg.validation.fn.Validator;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static buckelieg.validation.fn.Validator.ofPredicate;
import static java.util.Objects.requireNonNull;

/**
 * A collection of general purpose validators
 */
public final class Validators {

    private Validators() {
        throw new UnsupportedOperationException("No instances of Validators");
    }


    /**
     * Returns a validator for each element of provided collection
     *
     * @param validator a collection element validator
     * @param <T>       a collection element value type
     * @param <I>       a collection type
     * @return a <code>Validator</code> instance
     */
    public static <T, I extends Iterable<T>> Validator<I> eachOf(Validator<T> validator) {
        requireNonNull(validator, "Validator must be provided");
        return values -> {
            for (T value : values) {
                validator.validate(value);
            }
            return values;
        };
    }

    /**
     * Returns a validator for each element of provided collection based on provided predicate and error message supplier
     *
     * @param predicate       a validation test case
     * @param messageSupplier an error message supplier function
     * @param <T>             a collection element value type
     * @param <I>             a collection type
     * @return a <code>Validator</code> instance
     */
    public static <T, I extends Iterable<T>> Validator<I> eachOf(Predicate<T> predicate, Function<T, String> messageSupplier) {
        return eachOf(ofPredicate(predicate, messageSupplier));
    }

    /**
     * Returns a validator for each element of provided collection based on provided predicate and error message supplier
     *
     * @param predicate       a validation test case
     * @param messageSupplier an error message supplier function
     * @param <T>             a collection element value type
     * @param <I>             a collection type
     * @return a <code>Validator</code> instance
     */
    public static <T, I extends Iterable<T>> Validator<I> eachOf(Predicate<T> predicate, BiFunction<T, I, String> messageSupplier) {
        requireNonNull(messageSupplier, "Error message supplier function must be provided");
        return values -> {
            Validator<T> validator = ofPredicate(predicate, value -> messageSupplier.apply(value, values));
            for (T value : values) {
                validator.validate(value);
            }
            return values;
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
    public static <T> Validator<T> notNull(Supplier<String> messageSupplier) {
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
    public static <T> Validator<T> notNull(String errorMessage) {
        return notNull(() -> errorMessage);
    }

    /**
     * Constructs a {@linkplain Validator} instance that tests value for nullness with default error message of: <code>Provided value must not be null</code>
     *
     * @param <T> validated value type
     * @return a <code>Validator</code> instance
     */
    public static <T> Validator<T> notNull() {
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
    public static <T> Validator<T> isNull(Supplier<String> messageSupplier) {
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
    public static <T> Validator<T> isNull(String errorMessage) {
        return isNull(() -> errorMessage);
    }

    /**
     * Constructs a {@linkplain Validator} instance that tests value for nullness with default error message of: <code>Provided value must be null</code>
     *
     * @param <T> validated value type
     * @return a <code>Validator</code> instance
     */
    public static <T> Validator<T> isNull() {
        return isNull("Provided value must be null");
    }

    /**
     * Constructs a {@linkplain Validator} instance that tests value contained in the {@linkplain Optional} object if it is present
     *
     * @param validator a value validator
     * @param <T>       validated value type
     * @return a <code>Validator</code> instance
     */
    public static <T> Validator<Optional<T>> ifPresent(Validator<T> validator) {
        return Validator.<Optional<T>>of().thenMapIf(Optional::isPresent, Optional::get, validator);
    }

    /**
     * Constructs a {@linkplain Validator} instance that tests value contained in the {@linkplain Optional} object if it is present
     *
     * @param predicate       a validation test case
     * @param messageSupplier an error message supplier function
     * @param <T>             validated value type
     * @return a <code>Validator</code> instance
     */
    public static <T> Validator<Optional<T>> ifPresent(Predicate<T> predicate, Function<T, String> messageSupplier) {
        return Validator.<Optional<T>>of().thenMapIf(Optional::isPresent, Optional::get, ofPredicate(predicate, messageSupplier));
    }

    /**
     * Constructs a {@linkplain Validator} instance that tests value contained in the {@linkplain Optional} object if it is present
     *
     * @param predicate    a validation test case
     * @param errorMessage an error message
     * @param <T>          validated value type
     * @return a <code>Validator</code> instance
     */
    public static <T> Validator<Optional<T>> ifPresent(Predicate<T> predicate, String errorMessage) {
        return Validator.<Optional<T>>of().thenMapIf(Optional::isPresent, Optional::get, ofPredicate(predicate, errorMessage));
    }

}
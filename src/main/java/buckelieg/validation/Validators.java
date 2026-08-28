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

import buckelieg.fn.Validator;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static buckelieg.fn.Validator.rejectIf;
import static java.util.Objects.requireNonNull;

/**
 * Non-instantiable utility class containing general-purpose validator factories and combinators.
 */
public final class Validators {

    private Validators() {
        throw new AssertionError("No instances");
    }

    /**
     * Combines validators into one accumulating validator. Every validator is executed in declaration order,
     * even when an earlier validator fails. If one or more validators throw {@link ValidationException}, this
     * method throws a new aggregate exception containing every failure after all validators have run.
     * Exceptions other than {@code ValidationException} are propagated immediately.
     *
     * @param validators validators to execute
     * @param <T>        validated value type
     * @return a validator that returns the original value when every validator succeeds
     * @throws NullPointerException if the array or any validator is {@code null}
     * @see ValidationException#getExceptions()
     * @see ValidationException#getMessages()
     */
    @SafeVarargs
    public static <T> Validator<T> allOf(Validator<? super T>... validators) {
        requireNonNull(validators, "Validators must be provided");
        Validator<? super T>[] rules = validators.clone();
        for (Validator<? super T> validator : rules) {
            requireNonNull(validator, "Validator must be provided");
        }
        return value -> {
            ValidationException aggregate = new ValidationException();
            for (Validator<? super T> validator : rules) {
                try {
                    validator.validate(value);
                } catch (ValidationException exception) {
                    aggregate.addException(exception);
                }
            }
            if (!aggregate.getExceptions().isEmpty()) throw aggregate;
            return value;
        };
    }

    /**
     * Returns a validator for each element of provided collection
     *
     * @param validator a collection element validator
     * @param <T>       a collection element value type
     * @param <I>       a collection type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    public static <T, I extends Iterable<T>> Validator<I> eachOf(Validator<T> validator) {
        requireNonNull(validator, "Validator must be provided");
        return values -> {
            for (T value : values) validator.validate(value);
            return values;
        };
    }

    /**
     * Returns a validator that rejects an iterable when an element matches the failure predicate.
     *
     * @param predicate       predicate describing an invalid element in the iterable context
     * @param messageSupplier validation failure message supplier
     * @param <T>             element type
     * @param <I>             iterable type
     * @return an iterable validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, I extends Iterable<T>> Validator<I> eachRejectIf(BiPredicate<T, I> predicate, BiFunction<T, I, String> messageSupplier) {
        requireNonNull(predicate, "Predicate must be provided");
        requireNonNull(messageSupplier, "Error message supplier function must be provided");
        return values -> {
            Validator<T> validator = rejectIf(value -> predicate.test(value, values), value -> messageSupplier.apply(value, values));
            for (T value : values) validator.validate(value);
            return values;
        };
    }

    /**
     * Returns a validator that rejects an iterable when an element matches the failure predicate.
     *
     * @param predicate       failure predicate receiving an element and its iterable
     * @param messageSupplier failure message supplier receiving the rejected element
     * @param <T>             element type
     * @param <I>             iterable type
     * @return an iterable validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, I extends Iterable<T>> Validator<I> eachRejectIf(BiPredicate<T, I> predicate, Function<T, String> messageSupplier) {
        requireNonNull(messageSupplier, "Error message supplier function must be provided");
        return eachRejectIf(predicate, (value, values) -> messageSupplier.apply(value));
    }

    /**
     * Returns a validator that rejects an iterable when an element matches the failure predicate.
     *
     * @param predicate    failure predicate receiving an element and its iterable
     * @param errorMessage constant validation failure message
     * @param <T>          element type
     * @param <I>          iterable type
     * @return an iterable validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, I extends Iterable<T>> Validator<I> eachRejectIf(BiPredicate<T, I> predicate, String errorMessage) {
        String message = requireNonNull(errorMessage, "Error message must be provided");
        return eachRejectIf(predicate, value -> message);
    }

    /**
     * Returns a validator that requires every element to match the predicate in the iterable context.
     *
     * @param predicate       validity predicate receiving an element and its iterable
     * @param messageSupplier failure message supplier receiving the invalid element and its iterable
     * @param <T>             element type
     * @param <I>             iterable type
     * @return an iterable validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, I extends Iterable<T>> Validator<I> eachRequire(BiPredicate<T, I> predicate, BiFunction<T, I, String> messageSupplier) {
        BiPredicate<T, I> valid = requireNonNull(predicate, "Predicate must be provided");
        return eachRejectIf((value, values) -> !valid.test(value, values), messageSupplier);
    }

    /**
     * Returns a validator that requires every element to match the predicate in the iterable context.
     *
     * @param predicate       validity predicate receiving an element and its iterable
     * @param messageSupplier failure message supplier receiving the invalid element
     * @param <T>             element type
     * @param <I>             iterable type
     * @return an iterable validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, I extends Iterable<T>> Validator<I> eachRequire(BiPredicate<T, I> predicate, Function<T, String> messageSupplier) {
        BiPredicate<T, I> valid = requireNonNull(predicate, "Predicate must be provided");
        return eachRejectIf((value, values) -> !valid.test(value, values), messageSupplier);
    }

    /**
     * Returns a validator that requires every element to match the predicate in the iterable context.
     *
     * @param predicate    validity predicate receiving an element and its iterable
     * @param errorMessage constant validation failure message
     * @param <T>          element type
     * @param <I>          iterable type
     * @return an iterable validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, I extends Iterable<T>> Validator<I> eachRequire(BiPredicate<T, I> predicate, String errorMessage) {
        BiPredicate<T, I> valid = requireNonNull(predicate, "Predicate must be provided");
        return eachRejectIf((value, values) -> !valid.test(value, values), errorMessage);
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
        Supplier<String> supplier = requireNonNull(messageSupplier, "Error message supplier must be provided");
        return rejectIf(Objects::isNull, value -> supplier.get());
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
        String message = requireNonNull(errorMessage, "Error message must be provided");
        return notNull(() -> message);
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
     * @throws NullPointerException if <code>messageSupplier</code> is null
     */
    public static <T> Validator<T> isNull(Supplier<String> messageSupplier) {
        Supplier<String> supplier = requireNonNull(messageSupplier, "Error message supplier must be provided");
        return rejectIf(Objects::nonNull, value -> supplier.get());
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
        String message = requireNonNull(errorMessage, "Error message must be provided");
        return isNull(() -> message);
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
     * Constructs a {@linkplain Validator} instance that tests value contained in the {@linkplain Optional} object if:<br/>
     * 1) Optional itself is non-null object<br/>
     * 2) Optional.isPresent method returns <code>true</code><br/>
     * Otherwise no validations are made
     *
     * @param validator a value validator
     * @param <T>       validated value type
     * @return a <code>Validator</code> instance
     */
    public static <T> Validator<Optional<T>> ifPresent(Validator<T> validator) {
        return ifNotNullAnd(Optional::isPresent, map(Optional::get, validator));
    }

    /**
     * Constructs a <code>Validator</code> instance which checks value for null and if value is not null - applies provided validator
     *
     * @param validator a validator to apply
     * @param <T>       validated value type
     * @return a <code>Validator</code> instance
     */
    public static <T> Validator<T> ifNotNull(Validator<T> validator) {
        return ifNotNullAnd(Predicates.TRUE.predicate(), validator);
    }

    /**
     * Constructs a <code>Validator</code> instance which checks value for null AND applies another one provided with predicate
     *
     * @param condition an extra condition (besides non-nullness) to be applied
     * @param validator a validator to be executed
     * @param <T>       validated value type
     * @return a <code>Validator</code> instance
     * @throws NullPointerException if any argument is null
     */
    public static <T> Validator<T> ifNotNullAnd(Predicate<T> condition, Validator<T> validator) {
        requireNonNull(condition, "Condition predicate must be provided");
        requireNonNull(validator, "Validator must be provided");
        return value -> {
            if (null != value && condition.test(value)) validator.validate(value);
            return value;
        };
    }

    /**
     * Maps an input value and validates the mapped value with the provided validator.
     *
     * @param valueMapper mapped value function
     * @param validator   mapped value validator
     * @param <T>         input value type
     * @param <R>         mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> map(Function<T, R> valueMapper, Validator<R> validator) {
        requireNonNull(valueMapper, "Value valueMapper must be provided");
        requireNonNull(validator, "Validator must be provided");
        return value -> {
            validator.validate(valueMapper.apply(value));
            return value;
        };
    }

    /**
     * Maps a value and rejects it when the mapped-value predicate returns {@code true}.
     *
     * @param valueMapper     maps the input value
     * @param predicate       failure predicate receiving the mapped and original values
     * @param messageSupplier failure message supplier receiving the mapped and original values
     * @param <T>             input value type
     * @param <R>             mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> mapRejectIf(Function<T, R> valueMapper, BiPredicate<R, T> predicate, BiFunction<R, T, String> messageSupplier) {
        requireNonNull(valueMapper, "Key must be provided");
        requireNonNull(predicate, "Predicate must be provided");
        requireNonNull(messageSupplier, "Error message supplier function must be provided");
        return value -> {
            R mappedValue = valueMapper.apply(value);
            if (predicate.test(mappedValue, value))
                throw new ValidationException(messageSupplier.apply(mappedValue, value));
            return value;
        };
    }

    /**
     * Maps a value and rejects it when the mapped-value predicate returns {@code true}.
     *
     * @param valueMapper     maps the input value
     * @param predicate       failure predicate receiving the mapped and original values
     * @param messageSupplier failure message supplier receiving the mapped value
     * @param <T>             input value type
     * @param <R>             mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> mapRejectIf(Function<T, R> valueMapper, BiPredicate<R, T> predicate, Function<R, String> messageSupplier) {
        requireNonNull(messageSupplier, "Error message supplier function must be provided");
        return mapRejectIf(valueMapper, predicate, (mapped, original) -> messageSupplier.apply(mapped));
    }

    /**
     * Maps a value and rejects it when the mapped-value predicate returns {@code true}.
     *
     * @param valueMapper  maps the input value
     * @param predicate    failure predicate receiving the mapped and original values
     * @param errorMessage constant validation failure message
     * @param <T>          input value type
     * @param <R>          mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> mapRejectIf(Function<T, R> valueMapper, BiPredicate<R, T> predicate, String errorMessage) {
        String message = requireNonNull(errorMessage, "Error message must be provided");
        return mapRejectIf(valueMapper, predicate, value -> message);
    }

    /**
     * Maps a value and rejects it when the mapped-value predicate returns {@code true}.
     *
     * @param valueMapper     maps the input value
     * @param predicate       failure predicate receiving the mapped value
     * @param messageSupplier failure message supplier receiving the mapped and original values
     * @param <T>             input value type
     * @param <R>             mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> mapRejectIf(Function<T, R> valueMapper, Predicate<R> predicate, BiFunction<R, T, String> messageSupplier) {
        requireNonNull(predicate, "Predicate must be provided");
        return mapRejectIf(valueMapper, (mapped, original) -> predicate.test(mapped), messageSupplier);
    }

    /**
     * Maps a value and rejects it when the mapped-value predicate returns {@code true}.
     *
     * @param valueMapper     maps the input value
     * @param predicate       failure predicate receiving the mapped value
     * @param messageSupplier failure message supplier receiving the mapped value
     * @param <T>             input value type
     * @param <R>             mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> mapRejectIf(Function<T, R> valueMapper, Predicate<R> predicate, Function<R, String> messageSupplier) {
        requireNonNull(predicate, "Predicate must be provided");
        return mapRejectIf(valueMapper, (mapped, original) -> predicate.test(mapped), messageSupplier);
    }

    /**
     * Maps a value and rejects it when the mapped-value predicate returns {@code true}.
     *
     * @param valueMapper  maps the input value
     * @param predicate    failure predicate receiving the mapped value
     * @param errorMessage constant validation failure message
     * @param <T>          input value type
     * @param <R>          mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> mapRejectIf(Function<T, R> valueMapper, Predicate<R> predicate, String errorMessage) {
        requireNonNull(predicate, "Predicate must be provided");
        String message = requireNonNull(errorMessage, "Error message must be provided");
        return mapRejectIf(valueMapper, (mapped, original) -> predicate.test(mapped), value -> message);
    }

    /**
     * Maps the value and requires the mapped-value predicate to return {@code true}.
     *
     * @param valueMapper     maps the input value
     * @param predicate       validity predicate receiving the mapped and original values
     * @param messageSupplier failure message supplier receiving the mapped and original values
     * @param <T>             input value type
     * @param <R>             mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> mapRequire(Function<T, R> valueMapper, BiPredicate<R, T> predicate, BiFunction<R, T, String> messageSupplier) {
        BiPredicate<R, T> valid = requireNonNull(predicate, "Predicate must be provided");
        return mapRejectIf(valueMapper, (mapped, original) -> !valid.test(mapped, original), messageSupplier);
    }

    /**
     * Maps the value and requires the mapped-value predicate to return {@code true}.
     *
     * @param valueMapper     maps the input value
     * @param predicate       validity predicate receiving the mapped and original values
     * @param messageSupplier failure message supplier receiving the mapped value
     * @param <T>             input value type
     * @param <R>             mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> mapRequire(Function<T, R> valueMapper, BiPredicate<R, T> predicate, Function<R, String> messageSupplier) {
        BiPredicate<R, T> valid = requireNonNull(predicate, "Predicate must be provided");
        return mapRejectIf(valueMapper, (mapped, original) -> !valid.test(mapped, original), messageSupplier);
    }

    /**
     * Maps the value and requires the mapped-value predicate to return {@code true}.
     *
     * @param valueMapper  maps the input value
     * @param predicate    validity predicate receiving the mapped and original values
     * @param errorMessage constant validation failure message
     * @param <T>          input value type
     * @param <R>          mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> mapRequire(Function<T, R> valueMapper, BiPredicate<R, T> predicate, String errorMessage) {
        BiPredicate<R, T> valid = requireNonNull(predicate, "Predicate must be provided");
        return mapRejectIf(valueMapper, (mapped, original) -> !valid.test(mapped, original), errorMessage);
    }

    /**
     * Maps the value and requires the mapped-value predicate to return {@code true}.
     *
     * @param valueMapper     maps the input value
     * @param predicate       validity predicate receiving the mapped value
     * @param messageSupplier failure message supplier receiving the mapped and original values
     * @param <T>             input value type
     * @param <R>             mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> mapRequire(Function<T, R> valueMapper, Predicate<R> predicate, BiFunction<R, T, String> messageSupplier) {
        Predicate<R> valid = requireNonNull(predicate, "Predicate must be provided");
        return mapRejectIf(valueMapper, mapped -> !valid.test(mapped), messageSupplier);
    }

    /**
     * Maps the value and requires the mapped-value predicate to return {@code true}.
     *
     * @param valueMapper     maps the input value
     * @param predicate       validity predicate receiving the mapped value
     * @param messageSupplier failure message supplier receiving the mapped value
     * @param <T>             input value type
     * @param <R>             mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> mapRequire(Function<T, R> valueMapper, Predicate<R> predicate, Function<R, String> messageSupplier) {
        Predicate<R> valid = requireNonNull(predicate, "Predicate must be provided");
        return mapRejectIf(valueMapper, mapped -> !valid.test(mapped), messageSupplier);
    }

    /**
     * Maps the value and requires the mapped-value predicate to return {@code true}.
     *
     * @param valueMapper  maps the input value
     * @param predicate    validity predicate receiving the mapped value
     * @param errorMessage constant validation failure message
     * @param <T>          input value type
     * @param <R>          mapped value type
     * @return an input value validator
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Validator<T> mapRequire(Function<T, R> valueMapper, Predicate<R> predicate, String errorMessage) {
        Predicate<R> valid = requireNonNull(predicate, "Predicate must be provided");
        return mapRejectIf(valueMapper, mapped -> !valid.test(mapped), errorMessage);
    }

}

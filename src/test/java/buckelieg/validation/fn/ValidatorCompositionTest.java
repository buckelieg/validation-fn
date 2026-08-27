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

import buckelieg.fn.Validator;
import buckelieg.validation.ValidationException;
import buckelieg.validation.Validators;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ValidatorCompositionTest {

    private static <T> void assertFailure(String expectedMessage, Validator<T> validator, T value) {
        ValidationException exception = assertThrows(ValidationException.class, () -> validator.validate(value));
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void coreFactoriesCollectionAndPredicateAdaptersCoverSuccessAndFailure() {
        Validator<String> identity = Validator.of();
        assertSame(identity, Validator.of(identity));
        assertEquals("value", identity.validate("value"));

        Validator<String> built = Validator.build(validator -> validator.then(String::isEmpty, "empty"));
        assertEquals("value", built.validate("value"));
        assertFailure("empty", built, "");

        Validator<String> suppliedMessage = Validator.ofPredicate(String::isEmpty, value -> "invalid:" + value);
        assertFailure("invalid:", suppliedMessage, "");
        assertTrue(suppliedMessage.collect("").isPresent());
        assertFalse(suppliedMessage.collect("value").isPresent());
        assertTrue(suppliedMessage.toPredicate().test(""));
        assertFalse(suppliedMessage.toPredicate().test("value"));

        assertThrows(NullPointerException.class, () -> Validator.of(null));
        assertThrows(NullPointerException.class, () -> Validator.build(null));
        assertThrows(NullPointerException.class, () -> Validator.build(validator -> null));
        assertThrows(NullPointerException.class, () -> Validator.ofPredicate(null, "error"));
        assertThrows(NullPointerException.class, () -> Validator.ofPredicate(value -> false, (String) null));
        assertThrows(NullPointerException.class, () -> Validator.ofPredicate(value -> false, (Function<Object, String>) null));
    }

    @Test
    public void sequentialValidatorsRespectConditionsAndNullGuards() {
        AtomicInteger calls = new AtomicInteger();
        Validator<String> counter = value -> {
            calls.incrementAndGet();
            return value;
        };

        Validator.<String>of().thenIf(value -> false, counter).validate("value");
        assertEquals(0, calls.get());
        Validator.<String>of().thenIf(value -> true, counter).validate("value");
        assertEquals(1, calls.get());
        Validator.<String>of().then(counter).validate("value");
        assertEquals(2, calls.get());

        assertFailure("function:value", Validator.<String>of().then(value -> true, value -> "function:" + value), "value");
        assertFailure("constant", Validator.<String>of().then(value -> true, "constant"), "value");
        Validator.<String>of().thenIfNotNull(counter).validate(null);
        assertEquals(2, calls.get());
        assertFailure("nonnull:value", Validator.<String>of().thenIfNotNull(value -> true, value -> "nonnull:" + value), "value");
        assertFailure("nonnull", Validator.<String>of().thenIfNotNull(value -> true, "nonnull"), "value");

        assertThrows(NullPointerException.class, () -> Validator.<String>of().thenIf(null, counter));
        assertThrows(NullPointerException.class, () -> Validator.<String>of().thenIf(value -> true, null));
    }

    @Test
    public void conditionalMappingOverloadsPreserveMappedAndOriginalValues() {
        Function<String, Integer> length = String::length;
        Predicate<Integer> invalidLength = value -> value == 4;
        BiPredicate<Integer, String> invalidPair = (mapped, original) -> mapped == original.length();
        BiFunction<Integer, String, String> pairMessage = (mapped, original) -> mapped + ":" + original;

        Validator.<String>of().thenMapIf(value -> false, length,
                Validator.ofPredicate(invalidLength, "skipped")).validate("test");
        assertFailure("mapped-validator", Validator.<String>of().thenMapIf(value -> true, length,
                Validator.ofPredicate(invalidLength, "mapped-validator")), "test");
        assertFailure("4:test", Validator.<String>of().thenMapIf(value -> true, length, invalidPair, pairMessage), "test");
        assertFailure("mapped:4", Validator.<String>of().thenMapIf(value -> true, length, invalidPair,
                mapped -> "mapped:" + mapped), "test");
        assertFailure("bi-constant", Validator.<String>of().thenMapIf(value -> true, length, invalidPair,
                "bi-constant"), "test");
        assertFailure("4:test", Validator.<String>of().thenMapIf(value -> true, length, invalidLength, pairMessage), "test");
        assertFailure("mapped:4", Validator.<String>of().thenMapIf(value -> true, length, invalidLength,
                mapped -> "mapped:" + mapped), "test");
        assertFailure("predicate-constant", Validator.<String>of().thenMapIf(value -> true, length, invalidLength,
                "predicate-constant"), "test");
    }

    @Test
    public void unconditionalMappingOverloadsDelegateToConditionalComposition() {
        Function<String, Integer> length = String::length;
        Predicate<Integer> invalidLength = value -> value == 4;
        BiPredicate<Integer, String> invalidPair = (mapped, original) -> mapped == original.length();
        BiFunction<Integer, String, String> pairMessage = (mapped, original) -> mapped + ":" + original;

        assertFailure("mapped-validator", Validator.<String>of().thenMap(length,
                Validator.ofPredicate(invalidLength, "mapped-validator")), "test");
        assertFailure("4:test", Validator.<String>of().thenMap(length, invalidPair, pairMessage), "test");
        assertFailure("mapped:4", Validator.<String>of().thenMap(length, invalidPair,
                mapped -> "mapped:" + mapped), "test");
        assertFailure("bi-constant", Validator.<String>of().thenMap(length, invalidPair, "bi-constant"), "test");
        assertFailure("4:test", Validator.<String>of().thenMap(length, invalidLength, pairMessage), "test");
        assertFailure("mapped:4", Validator.<String>of().thenMap(length, invalidLength,
                mapped -> "mapped:" + mapped), "test");
        assertFailure("predicate-constant", Validator.<String>of().thenMap(length, invalidLength,
                "predicate-constant"), "test");
    }

    @Test
    public void nonNullMappingOverloadsSkipNullAndValidatePresentValues() {
        Function<String, Integer> length = String::length;
        Predicate<Integer> invalidLength = value -> value == 4;
        BiPredicate<Integer, String> invalidPair = (mapped, original) -> mapped == original.length();
        BiFunction<Integer, String, String> pairMessage = (mapped, original) -> mapped + ":" + original;

        assertEquals(null, Validator.<String>of().thenMapIfNotNull(length,
                Validator.ofPredicate(invalidLength, "mapped-validator")).validate(null));
        assertFailure("mapped-validator", Validator.<String>of().thenMapIfNotNull(length,
                Validator.ofPredicate(invalidLength, "mapped-validator")), "test");
        assertFailure("4:test", Validator.<String>of().thenMapIfNotNull(length, invalidPair, pairMessage), "test");
        assertFailure("mapped:4", Validator.<String>of().thenMapIfNotNull(length, invalidPair,
                mapped -> "mapped:" + mapped), "test");
        assertFailure("bi-constant", Validator.<String>of().thenMapIfNotNull(length, invalidPair,
                "bi-constant"), "test");
        assertFailure("4:test", Validator.<String>of().thenMapIfNotNull(length, invalidLength, pairMessage), "test");
        assertFailure("mapped:4", Validator.<String>of().thenMapIfNotNull(length, invalidLength,
                mapped -> "mapped:" + mapped), "test");
        assertFailure("predicate-constant", Validator.<String>of().thenMapIfNotNull(length, invalidLength,
                "predicate-constant"), "test");
    }

    @Test
    public void nullOptionalAndConditionalFactoriesCoverGuardedExecution() {
        assertFailure("required", Validators.notNull(() -> "required"), null);
        assertFailure("required-string", Validators.notNull("required-string"), null);
        assertFailure("Provided value must not be null", Validators.notNull(), null);
        assertFailure("must-be-null", Validators.isNull(() -> "must-be-null"), "value");
        assertFailure("must-be-null-string", Validators.isNull("must-be-null-string"), "value");
        assertFailure("Provided value must be null", Validators.isNull(), "value");

        Validators.ifPresent(Validator.ofPredicate(String::isEmpty, "empty")).validate(Optional.empty());
        assertFailure("empty", Validators.ifPresent(String::isEmpty, value -> "empty"), Optional.of(""));
        assertFailure("empty-constant", Validators.ifPresent(String::isEmpty, "empty-constant"), Optional.of(""));

        Validators.ifNotNull(Validator.ofPredicate(String::isEmpty, "empty")).validate(null);
        assertFailure("empty", Validators.ifNotNull(String::isEmpty, value -> "empty"), "");
        assertFailure("empty-constant", Validators.ifNotNull(String::isEmpty, "empty-constant"), "");

        Validators.ifNotNullAnd(value -> false, Validator.ofPredicate(value -> true, "skipped")).validate("value");
        assertFailure("conditional:value", Validators.ifNotNullAnd(value -> true, value -> true,
                value -> "conditional:" + value), "value");
        assertFailure("conditional-constant", Validators.ifNotNullAnd(value -> true, value -> true,
                "conditional-constant"), "value");

        assertFailure("required:", Validators.notNullOr(String::isEmpty, value -> "required:" + value), "");
        assertFailure("required", Validators.notNullOr(String::isEmpty, "required"), null);
        Validators.notNullOr(String::isEmpty, "required").validate("value");
    }

    @Test
    public void eachOfFactoriesCoverEveryMessageSupplierShape() {
        List<String> values = Arrays.asList("ok", "");
        Validator<String> elementValidator = Validator.ofPredicate(String::isEmpty, "element");

        assertFailure("element", Validators.eachOf(elementValidator), values);
        assertFailure("0/2", Validators.eachOf((value, all) -> value.isEmpty(),
                (value, all) -> value.length() + "/" + ((List<?>) all).size()), values);
        assertFailure("value:", Validators.eachOf((value, all) -> value.isEmpty(),
                value -> "value:" + value), values);
        assertFailure("bi-constant", Validators.eachOf((value, all) -> value.isEmpty(), "bi-constant"), values);
        assertFailure("0/2", Validators.eachOf(String::isEmpty,
                (value, all) -> value.length() + "/" + ((List<?>) all).size()), values);
        assertFailure("value:", Validators.eachOf(String::isEmpty, value -> "value:" + value), values);
        assertFailure("constant", Validators.eachOf(String::isEmpty, "constant"), values);
        assertEquals(Collections.emptyList(), Validators.eachOf(elementValidator).validate(Collections.emptyList()));
        assertEquals(Collections.singletonList("ok"), Validators.<String, List<String>>eachOf(
                (value, all) -> value.isEmpty(),
                (value, all) -> "invalid"
        ).validate(Collections.singletonList("ok")));
    }

    @Test
    public void mapFactoriesCoverEveryPredicateAndMessageShape() {
        Function<String, Integer> length = String::length;
        Predicate<Integer> invalidLength = value -> value == 4;
        BiPredicate<Integer, String> invalidPair = (mapped, original) -> mapped == original.length();
        BiFunction<Integer, String, String> pairMessage = (mapped, original) -> mapped + ":" + original;

        assertFailure("mapped-validator", Validators.map(length,
                Validator.ofPredicate(invalidLength, "mapped-validator")), "test");
        assertFailure("4:test", Validators.map(length, invalidPair, pairMessage), "test");
        assertFailure("mapped:4", Validators.map(length, invalidPair, mapped -> "mapped:" + mapped), "test");
        assertFailure("bi-constant", Validators.map(length, invalidPair, "bi-constant"), "test");
        assertFailure("4:test", Validators.map(length, invalidLength, pairMessage), "test");
        assertFailure("mapped:4", Validators.map(length, invalidLength, mapped -> "mapped:" + mapped), "test");
        assertFailure("predicate-constant", Validators.map(length, invalidLength, "predicate-constant"), "test");

        assertThrows(NullPointerException.class, () -> Validators.map(null, Validator.of()));
        assertThrows(NullPointerException.class, () -> Validators.map(length, (Validator<Integer>) null));
        assertThrows(NullPointerException.class, () -> Validators.ifPresent((Validator<Object>) null));
        assertThrows(NullPointerException.class, () -> Validators.ifNotNullAnd(null, Validator.of()));
    }
}

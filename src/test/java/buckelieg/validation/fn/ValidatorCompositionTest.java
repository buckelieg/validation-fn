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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ValidatorCompositionTest {

    private static <T> void assertFailure(String expectedMessage, Validator<T> validator, T value) {
        ValidationException exception = assertThrows(ValidationException.class, () -> validator.validate(value));
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void explicitFactoriesSeparateValidityFromFailurePredicates() {
        Predicate<Object> nonNull = value -> value != null;
        Function<Object, String> requiredMessage = value -> "required:" + value;

        assertEquals("value", Validator.<String>require(nonNull, requiredMessage).validate("value"));
        assertFailure("required:null", Validator.<String>require(nonNull, requiredMessage), null);
        assertFailure("required", Validator.require(nonNull, "required"), null);

        assertEquals("value", Validator.rejectIf(String::isEmpty, "empty").validate("value"));
        assertFailure("empty:", Validator.rejectIf(String::isEmpty, value -> "empty:" + value), "");
        assertFailure("empty", Validator.rejectIf(String::isEmpty, "empty"), "");

        assertThrows(NullPointerException.class, () -> Validator.require(null, "required"));
        assertThrows(NullPointerException.class, () -> Validator.require(nonNull, (String) null));
        assertThrows(NullPointerException.class, () -> Validator.rejectIf(null, "invalid"));
        assertThrows(NullPointerException.class, () -> Validator.rejectIf(nonNull, (Function<Object, String>) null));
    }

    @Test
    public void explicitSequentialMethodsReadInTheirBooleanDirection() {
        Validator<String> validator = Validator.<String>of()
                .thenRequire(value -> value.length() >= 3, value -> "short:" + value)
                .thenRequire(value -> value.length() <= 5, "long")
                .thenRejectIf(String::isEmpty, value -> "empty:" + value)
                .thenRejectIf(value -> value.contains("!"), "punctuation");

        assertEquals("valid", validator.validate("valid"));
        assertFailure("short:ab", validator, "ab");
        assertFailure("long", validator, "lengthy");
        assertFailure("punctuation", validator, "bad!");
    }

    @Test
    public void explicitMappedMethodsSupportAllMessageAndPredicateShapes() {
        Function<String, Integer> length = String::length;
        Predicate<Integer> validLength = value -> value == 4;
        Predicate<Integer> invalidLength = value -> value == 4;
        BiPredicate<Integer, String> validPair = (mapped, original) -> mapped == 4 && original.startsWith("t");
        BiPredicate<Integer, String> invalidPair = (mapped, original) -> mapped == original.length();
        BiFunction<Integer, String, String> pairMessage = (mapped, original) -> mapped + ":" + original;

        assertFailure("3:abc", Validator.<String>of().thenMapRequire(length, validPair, pairMessage), "abc");
        assertFailure("mapped:3", Validator.<String>of().thenMapRequire(length, validPair, mapped -> "mapped:" + mapped), "abc");
        assertFailure("pair-required", Validator.<String>of().thenMapRequire(length, validPair, "pair-required"), "abc");
        assertFailure("3:abc", Validator.<String>of().thenMapRequire(length, validLength, pairMessage), "abc");
        assertFailure("mapped:3", Validator.<String>of().thenMapRequire(length, validLength, mapped -> "mapped:" + mapped), "abc");
        assertFailure("required", Validator.<String>of().thenMapRequire(length, validLength, "required"), "abc");

        assertFailure("4:test", Validator.<String>of().thenMapRejectIf(length, invalidPair, pairMessage), "test");
        assertFailure("mapped:4", Validator.<String>of().thenMapRejectIf(length, invalidPair, mapped -> "mapped:" + mapped), "test");
        assertFailure("pair-rejected", Validator.<String>of().thenMapRejectIf(length, invalidPair, "pair-rejected"), "test");
        assertFailure("4:test", Validator.<String>of().thenMapRejectIf(length, invalidLength, pairMessage), "test");
        assertFailure("mapped:4", Validator.<String>of().thenMapRejectIf(length, invalidLength, mapped -> "mapped:" + mapped), "test");
        assertFailure("rejected", Validator.<String>of().thenMapRejectIf(length, invalidLength, "rejected"), "test");

        assertEquals("test", Validator.<String>of().thenMapRequire(length, validPair, pairMessage).validate("test"));
        assertEquals("test", Validator.<String>of().thenMapRequire(length, validPair, mapped -> "mapped:" + mapped).validate("test"));
        assertEquals("test", Validator.<String>of().thenMapRequire(length, validPair, "required").validate("test"));
        assertEquals("test", Validator.<String>of().thenMapRequire(length, validLength, pairMessage).validate("test"));
        assertEquals("test", Validator.<String>of().thenMapRequire(length, validLength, mapped -> "mapped:" + mapped).validate("test"));
        assertEquals("test", Validator.<String>of().thenMapRequire(length, validLength, "required").validate("test"));
        assertEquals("abc", Validator.<String>of().thenMapRejectIf(length, invalidLength, "rejected").validate("abc"));
        assertThrows(NullPointerException.class, () -> Validator.<String>of().thenMapRequire(length, (Predicate<Integer>) null, "required"));
        assertThrows(NullPointerException.class, () -> Validator.<String>of().thenMapRejectIf(length, invalidLength, (String) null));
    }

    @Test
    public void coreFactoriesCollectionAndPredicateAdaptersCoverSuccessAndFailure() {
        Validator<String> identity = Validator.of();
        assertSame(identity, Validator.of(identity));
        assertEquals("value", identity.validate("value"));

        Validator<String> built = Validator.build(validator -> validator.thenRejectIf(String::isEmpty, "empty"));
        assertEquals("value", built.validate("value"));
        assertFailure("empty", built, "");

        Validator<String> suppliedMessage = Validator.rejectIf(String::isEmpty, value -> "invalid:" + value);
        assertFailure("invalid:", suppliedMessage, "");
        assertTrue(suppliedMessage.collect("").isPresent());
        assertFalse(suppliedMessage.collect("value").isPresent());
        assertFalse(suppliedMessage.asValidPredicate().test(""));
        assertTrue(suppliedMessage.asValidPredicate().test("value"));
        assertTrue(suppliedMessage.asInvalidPredicate().test(""));
        assertFalse(suppliedMessage.asInvalidPredicate().test("value"));

        assertThrows(NullPointerException.class, () -> Validator.of(null));
        assertThrows(NullPointerException.class, () -> Validator.build(null));
        assertThrows(NullPointerException.class, () -> Validator.build(validator -> null));
        assertThrows(NullPointerException.class, () -> Validator.rejectIf(null, "error"));
        assertThrows(NullPointerException.class, () -> Validator.rejectIf(value -> false, (String) null));
        assertThrows(NullPointerException.class, () -> Validator.rejectIf(value -> false, (Function<Object, String>) null));
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

        assertFailure("function:value", Validator.<String>of().thenRejectIf(value -> true, value -> "function:" + value), "value");
        assertFailure("constant", Validator.<String>of().thenRejectIf(value -> true, "constant"), "value");
        Validator.<String>of().thenIfNotNull(counter).validate(null);
        assertEquals(2, calls.get());
        assertFailure("nonnull:value", Validator.<String>of().thenIfNotNull(Validator.rejectIf(value -> true, value -> "nonnull:" + value)), "value");
        assertFailure("nonnull", Validator.<String>of().thenIfNotNull(Validator.rejectIf(value -> true, "nonnull")), "value");

        assertThrows(NullPointerException.class, () -> Validator.<String>of().thenIf(null, counter));
        assertThrows(NullPointerException.class, () -> Validator.<String>of().thenIf(value -> true, null));
    }

    @Test
    public void conditionalMappingUsesExplicitMappedValidator() {
        Function<String, Integer> length = String::length;
        Predicate<Integer> invalidLength = value -> value == 4;

        Validator.<String>of().thenMapIf(value -> false, length, Validator.rejectIf(invalidLength, "skipped")).validate("test");
        assertFailure("mapped-validator", Validator.<String>of().thenMapIf(value -> true, length, Validator.rejectIf(invalidLength, "mapped-validator")), "test");
    }

    @Test
    public void validatorBasedMappingDelegatesToMappedValidator() {
        Function<String, Integer> length = String::length;
        Predicate<Integer> invalidLength = value -> value == 4;

        assertFailure("mapped-validator", Validator.<String>of().thenMap(length, Validator.rejectIf(invalidLength, "mapped-validator")), "test");
    }

    @Test
    public void nonNullMappingSkipsNullAndValidatesPresentValues() {
        Function<String, Integer> length = String::length;
        Predicate<Integer> invalidLength = value -> value == 4;

      assertNull(Validator.<String>of().thenMapIfNotNull(length, Validator.rejectIf(invalidLength, "mapped-validator")).validate(null));
        assertFailure("mapped-validator", Validator.<String>of().thenMapIfNotNull(length, Validator.rejectIf(invalidLength, "mapped-validator")), "test");
    }

    @Test
    public void nullOptionalAndConditionalFactoriesCoverGuardedExecution() {
        assertFailure("required", Validators.notNull(() -> "required"), null);
        assertFailure("required-string", Validators.notNull("required-string"), null);
        assertFailure("Provided value must not be null", Validators.notNull(), null);
        assertFailure("must-be-null", Validators.isNull(() -> "must-be-null"), "value");
        assertFailure("must-be-null-string", Validators.isNull("must-be-null-string"), "value");
        assertFailure("Provided value must be null", Validators.isNull(), "value");

        Validators.ifPresent(Validator.rejectIf(String::isEmpty, "empty")).validate(Optional.empty());
        assertFailure("empty", Validators.ifPresent(Validator.rejectIf(String::isEmpty, value -> "empty")), Optional.of(""));
        assertFailure("empty-constant", Validators.ifPresent(Validator.rejectIf(String::isEmpty, "empty-constant")), Optional.of(""));

        Validators.ifNotNull(Validator.rejectIf(String::isEmpty, "empty")).validate(null);
        assertFailure("empty", Validators.ifNotNull(Validator.rejectIf(String::isEmpty, value -> "empty")), "");
        assertFailure("empty-constant", Validators.ifNotNull(Validator.rejectIf(String::isEmpty, "empty-constant")), "");

        Validators.ifNotNullAnd(value -> false, Validator.rejectIf(value -> true, "skipped")).validate("value");
        assertFailure("conditional:value", Validators.ifNotNullAnd(value -> true, Validator.rejectIf(value -> true, value -> "conditional:" + value)), "value");
        assertFailure("conditional-constant", Validators.ifNotNullAnd(value -> true, Validator.rejectIf(value -> true, "conditional-constant")), "value");
    }

    @Test
    public void eachOfFactoriesCoverEveryMessageSupplierShape() {
        List<String> values = Arrays.asList("ok", "");
        Validator<String> elementValidator = Validator.rejectIf(String::isEmpty, "element");
        BiPredicate<String, List<String>> invalid = (value, all) -> value.isEmpty();
        BiPredicate<String, List<String>> valid = (value, all) -> !value.isEmpty();

        assertFailure("element", Validators.eachOf(elementValidator), values);
        assertFailure("0/2", Validators.eachRejectIf(invalid, (value, all) -> value.length() + "/" + all.size()), values);
        assertFailure("value:", Validators.eachRejectIf(invalid, value -> "value:" + value), values);
        assertFailure("reject-constant", Validators.eachRejectIf(invalid, "reject-constant"), values);
        assertFailure("0/2", Validators.eachRequire(valid, (value, all) -> value.length() + "/" + all.size()), values);
        assertFailure("value:", Validators.eachRequire(valid, value -> "value:" + value), values);
        assertFailure("require-constant", Validators.eachRequire(valid, "require-constant"), values);
        assertEquals(Collections.emptyList(), Validators.eachOf(elementValidator).validate(Collections.emptyList()));
        assertEquals(Collections.singletonList("ok"), Validators.eachRequire(valid, (value, all) -> "invalid").validate(Collections.singletonList("ok")));
    }

    @Test
    public void mapFactoriesCoverEveryPredicateAndMessageShape() {
        Function<String, Integer> length = String::length;
        Predicate<Integer> invalidLength = value -> value == 4;
        Predicate<Integer> validLength = value -> value == 4;
        BiPredicate<Integer, String> invalidPair = (mapped, original) -> mapped == original.length();
        BiPredicate<Integer, String> validPair = (mapped, original) -> mapped == 4 && original.startsWith("t");
        BiFunction<Integer, String, String> pairMessage = (mapped, original) -> mapped + ":" + original;

        assertFailure("mapped-validator", Validators.map(length, Validator.rejectIf(invalidLength, "mapped-validator")), "test");
        assertFailure("4:test", Validators.mapRejectIf(length, invalidPair, pairMessage), "test");
        assertFailure("mapped:4", Validators.mapRejectIf(length, invalidPair, mapped -> "mapped:" + mapped), "test");
        assertFailure("bi-constant", Validators.mapRejectIf(length, invalidPair, "bi-constant"), "test");
        assertFailure("4:test", Validators.mapRejectIf(length, invalidLength, pairMessage), "test");
        assertFailure("mapped:4", Validators.mapRejectIf(length, invalidLength, mapped -> "mapped:" + mapped), "test");
        assertFailure("predicate-constant", Validators.mapRejectIf(length, invalidLength, "predicate-constant"), "test");
        assertFailure("3:abc", Validators.mapRequire(length, validPair, pairMessage), "abc");
        assertFailure("mapped:3", Validators.mapRequire(length, validPair, mapped -> "mapped:" + mapped), "abc");
        assertFailure("bi-required", Validators.mapRequire(length, validPair, "bi-required"), "abc");
        assertFailure("3:abc", Validators.mapRequire(length, validLength, pairMessage), "abc");
        assertFailure("mapped:3", Validators.mapRequire(length, validLength, mapped -> "mapped:" + mapped), "abc");
        assertFailure("predicate-required", Validators.mapRequire(length, validLength, "predicate-required"), "abc");

        assertThrows(NullPointerException.class, () -> Validators.map(null, Validator.of()));
        assertThrows(NullPointerException.class, () -> Validators.map(length, null));
        assertThrows(NullPointerException.class, () -> Validators.ifPresent(null));
        assertThrows(NullPointerException.class, () -> Validators.ifNotNullAnd(null, Validator.of()));
    }

    @Test
    public void constantMessageFactoriesRejectNullAtConstruction() {
        BiPredicate<String, List<String>> elementPredicate = (value, values) -> value.isEmpty();
        BiPredicate<String, List<String>> elementRequirement = (value, values) -> !value.isEmpty();
        Function<String, Integer> length = String::length;
        Predicate<Integer> mappedPredicate = value -> value == 4;
        BiPredicate<Integer, String> mappedBiPredicate = (mapped, original) -> mapped == original.length();

        assertThrows(NullPointerException.class, () -> Validators.eachRejectIf(elementPredicate, (String) null));
        assertThrows(NullPointerException.class, () -> Validators.eachRequire(elementRequirement, (String) null));
        assertThrows(NullPointerException.class, () -> Validators.notNull((String) null));
        assertThrows(NullPointerException.class, () -> Validators.isNull((String) null));
        assertThrows(NullPointerException.class, () -> Validators.mapRejectIf(length, mappedBiPredicate, (String) null));
        assertThrows(NullPointerException.class, () -> Validators.mapRejectIf(length, mappedPredicate, (String) null));
        assertThrows(NullPointerException.class, () -> Validators.mapRequire(length, mappedBiPredicate, (String) null));
        assertThrows(NullPointerException.class, () -> Validators.mapRequire(length, mappedPredicate, (String) null));
    }
}

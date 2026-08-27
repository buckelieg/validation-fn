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
import buckelieg.validation.Maps;
import buckelieg.validation.Numbers;
import buckelieg.validation.Predicates;
import buckelieg.validation.Strings;
import buckelieg.validation.ValidationException;
import buckelieg.validation.Validators;
import org.junit.Test;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CoreContractsTest {

    @Test
    public void nullValidatorMessageIsEvaluatedOnlyOnFailure() {
        AtomicInteger calls = new AtomicInteger();
        Validator<String> validator = Validators.notNull(() -> {
            calls.incrementAndGet();
            return "required";
        });

        assertEquals(0, calls.get());
        validator.validate("value");
        assertEquals(0, calls.get());
        assertEquals("required", assertThrows(ValidationException.class, () -> validator.validate(null)).getMessage());
        assertEquals(1, calls.get());
    }

    @Test
    public void validationExceptionAggregatesNestedMessages() {
        ValidationException root = new ValidationException();
        ValidationException nested = new ValidationException("address invalid");
        nested.addException(new ValidationException("city required"));
        root.addException(new ValidationException("name required"));
        root.addException(nested);

        String separator = System.lineSeparator();
        assertEquals("name required" + separator + "address invalid" + separator + "city required", root.getMessages());
        assertEquals(2, root.getExceptions().size());
        assertThrows(UnsupportedOperationException.class, () -> root.getExceptions().clear());
        assertThrows(IllegalArgumentException.class, () -> root.addException(root));
        assertThrows(IllegalArgumentException.class, () -> nested.addException(root));
    }

    @Test
    public void validationExceptionRejectsInvalidMessagesAndSkipsEmptyNestedOnes() {
        assertThrows(NullPointerException.class, () -> new ValidationException(null));
        assertThrows(IllegalArgumentException.class, () -> new ValidationException("  \t"));

        ValidationException root = new ValidationException();
        root.addException(new ValidationException());
        assertEquals("", root.getMessages());
        assertThrows(NullPointerException.class, () -> root.addException(null));
    }

    @Test
    public void caseInsensitivePredicatesAreLocaleIndependent() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            assertTrue(Strings.containsIgnoreCase("I").test("i"));
            assertTrue(Strings.startsWithIgnoreCase("I").test("istanbul"));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void characterPredicatesRejectEmptyStrings() {
        assertFalse(Strings.isNumeric(""));
        assertFalse(Strings.isAlphabetic(""));
        assertFalse(Strings.isAlphanumeric(""));
        assertTrue(Strings.isNumeric("123"));
    }

    @Test
    public void factoriesRejectInvalidArgumentsAtConstruction() {
        assertFalse(Numbers.isNumber(null));
        assertThrows(NullPointerException.class, () -> Predicates.gt(null));
        assertThrows(NullPointerException.class, () -> Strings.contains((String) null));
        assertThrows(IllegalArgumentException.class, () -> Maps.sizeOf(-1));
        assertThrows(NullPointerException.class, () -> Maps.keyValue("key", null));
    }
}

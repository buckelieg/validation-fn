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

import buckelieg.validation.Classes;
import org.junit.Test;

import java.io.Serializable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ClassesTest {

    interface Parent {
    }

    interface Child extends Parent {
    }

    interface Other {
    }

    static class Implementation implements Child, Other {
    }

    @Test
    public void typePredicatesHandleInstancesAndNulls() {
        assertTrue(Classes.isOfType(Number.class).test(42));
        assertFalse(Classes.isOfType(Number.class).test(null));
        assertTrue(Classes.isExact(String.class).test("value"));
        assertFalse(Classes.isExact(String.class).test(null));
        assertFalse(Classes.isExact(Number.class).test(42));
        assertTrue(Classes.isInstanceOf(Number.class).test(42));
        assertFalse(Classes.isInstanceOf(Number.class).test(null));
    }

    @Test
    public void classShapePredicatesAcceptInstancesAndClassTokens() {
        assertTrue(Classes.isArray(new String[0]));
        assertTrue(Classes.isArray(String[].class));
        assertTrue(Classes.<Object>isArray().test(new String[0]));
        assertFalse(Classes.<Object>isArray().test(null));
        assertTrue(Classes.isArrayOfType(CharSequence.class).test(new String[0]));
        assertFalse(Classes.isArrayOfType(Number.class).test(new String[0]));
        assertFalse(Classes.isArrayOfType(CharSequence.class).test("value"));
        assertFalse(Classes.isArrayOfType(CharSequence.class).test(null));
        assertFalse(Classes.isArray("value"));
        assertTrue(Classes.isPrimitive(int.class));
        assertFalse(Classes.isPrimitive(Integer.class));
        assertTrue(Classes.<Object>isPrimitive().test(int.class));
        assertFalse(Classes.<Object>isPrimitive().test(null));
        assertTrue(Classes.isInterface(Parent.class));
        assertFalse(Classes.isInterface(Implementation.class));
        assertTrue(Classes.<Object>isInterface().test(Parent.class));
        assertFalse(Classes.<Object>isInterface().test(null));
    }

    @Test
    public void interfacePredicatesImplementTheirNamedQuantifiers() {
        Implementation value = new Implementation();

        assertTrue(Classes.hasInterface(Parent.class).test(value));
        assertTrue(Classes.implementsAll(Parent.class, Other.class).test(value));
        assertTrue(Classes.implementsAllExact(Child.class, Other.class).test(value));
        assertTrue(Classes.implementsAny(Serializable.class, Other.class).test(value));
        assertFalse(Classes.implementsAny(Serializable.class).test(value));
        assertFalse(Classes.hasInterface(Parent.class).test(null));
        assertFalse(Classes.hasInterface(Parent.class).test(new Object()));
        assertFalse(Classes.implementsAll(Parent.class).test(null));
        assertFalse(Classes.implementsAll(Serializable.class).test(value));
        assertFalse(Classes.implementsAllExact(Parent.class).test(null));
        assertFalse(Classes.implementsAllExact(Serializable.class).test(value));
        assertFalse(Classes.implementsAny(Other.class).test(null));
    }

    @Test
    public void interfacePredicatesRejectClasses() {
        assertThrows(IllegalArgumentException.class, () -> Classes.hasInterface(String.class));
        assertThrows(IllegalArgumentException.class, () -> Classes.implementsAll(String.class));
        assertThrows(IllegalArgumentException.class, () -> Classes.implementsAny(String.class));
        assertThrows(NullPointerException.class, () -> Classes.isInstanceOf(null));
        assertThrows(NullPointerException.class, () -> Classes.isArrayOfType(null));
        assertThrows(NullPointerException.class, () -> Classes.implementsAll((Class<?>[]) null));
        assertThrows(NullPointerException.class, () -> Classes.implementsAny(Parent.class, null));
    }
}

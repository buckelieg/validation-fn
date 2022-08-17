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

import buckelieg.validation.Predicates;
import buckelieg.validation.ValidationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

//TODO write more test cases here...
public class ValidatorTest {

    public static final String NULL_NOR_EMPTY = "stringProperty must not be null nor empty";

    static class MyClass {
        private final String stringProperty;
        private int number;

        public MyClass(String stringProperty) {
            this.stringProperty = stringProperty;
        }

        public MyClass() {
            this(null);
        }

        public String getStringProperty() {
            return stringProperty;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }

    @Test
    public void test() {
        MyClass myClass = new MyClass("");
        Validator<MyClass> validator = Validator.<MyClass>empty()
                .thenMapIfNotNull(
                        MyClass::getStringProperty,
                        Predicates.<String>of(Objects::isNull).or(String::isEmpty),
                        NULL_NOR_EMPTY
                );
        Assert.assertEquals(
                NULL_NOR_EMPTY,
                Assert.assertThrows(
                        ValidationException.class,
                        () -> validator.validate(myClass)
                ).getMessage()
        );
    }
}

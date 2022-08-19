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

import buckelieg.validation.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;
import java.util.Optional;

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
        Validator<MyClass> validator = Validator.<MyClass>of()
                .thenMapIfNotNull(
                        MyClass::getStringProperty,
                        Predicates.<String>of(Objects::isNull).or(String::isEmpty),
                        NULL_NOR_EMPTY
                )
                .thenMap(
                        MyClass::getNumber,
                        Numbers::isNegative,
                        "Not negative"
                );
        Assert.assertEquals(
                NULL_NOR_EMPTY,
                Assert.assertThrows(
                        ValidationException.class,
                        () -> validator.validate(myClass)
                ).getMessage()
        );
    }

    @Test
    public void testNestedValidators() {
        Address address = new Address("MyCity", "MyStreet", 13);
        Person person = Validators.<Person>notNull("Person must be provided")
                .thenMap(
                        Person::getFirstName,
                        Predicates.of(Strings::isBlank).and(Strings.minLength(6)),
                        value -> String.format("FirstName '%s' must not be null and at least 6 characters long", value)
                )
                .thenMap(Person::getSecondName, Validator.<String>of().thenIf(
                        Predicates.of(Strings::isBlank).negate(),
                        Validator.ofPredicate(Strings.minLength(6), "Minimum second name length is 6")
                ))
                .thenMap(Person::getLastName, Strings::isBlank, "Last name must not be empty")
                .thenMap(
                        Person::getAge,
                        Predicates.<Integer>of(Numbers::isNegative).or(Numbers.max(100)),
                        "Age has to be greater than 0 and less than 100"
                )
                .thenMap(Person::getAddress, Validator.<Address>of()
                        .thenMap(Address::getCity, Strings::isBlank, "City must not be blank")
                        .thenMap(Address::getStreet, Strings::isBlank, "Street must not be blank")
                        .thenMap(Address::getBuildingNumber, Numbers::isNegative, "Build number must be positive")
                )
                .thenMap(Person::getAddress, Validator.<Address>of().then(
                        addr -> Strings.isBlank(addr.getCity()) || Strings.isBlank(addr.getStreet()) || Numbers.isNegative(addr.getBuildingNumber()),
                        "Address must be fully filled in"
                ))
                .thenMap(
                        Person::getNicknames,
                        Validators.eachOf(Strings::isBlank, (val, col) -> "Nickname must bot be blank " + col)
                )
                .thenMapIfNotNull(
                        Person::getGender,
                        Validator.<Optional<String>>of()
                                .thenIfNotNull(Validators.ifPresent(Strings::isBlank, ""))
                )
                .validate(
                        new Person("FirstName", "SecondName", "LastName", 76, address, "1", "1")
                );

    }
}

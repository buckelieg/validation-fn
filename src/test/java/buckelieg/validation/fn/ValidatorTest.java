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

import buckelieg.validation.Numbers;
import buckelieg.validation.Strings;
import buckelieg.validation.ValidationException;
import buckelieg.validation.Validators;
import org.junit.Test;

import java.util.Optional;

import static buckelieg.validation.Validators.*;
import static buckelieg.validation.fn.Validator.ofPredicate;
import static org.junit.Assert.*;

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
        Validator<MyClass> validator = Validator.build(noop -> noop
                .thenMapIfNotNull(MyClass::getStringProperty, Validators.notNullOr(String::isEmpty, NULL_NOR_EMPTY))
                .thenMap(MyClass::getNumber, Numbers::isNegative, "Not negative")
        );
        assertEquals(
                NULL_NOR_EMPTY,
                assertThrows(
                        ValidationException.class,
                        () -> validator.validate(new MyClass(""))
                ).getMessage()
        );
    }

    @Test
    public void testNestedValidators() {
        Address address1 = new Address("MyCity", "MyStreet", 13);
        Address address2 = new Address();
        Validator<Person> validator = Validators.<Person>notNull("Person must be provided")
                .thenMap(
                        Person::getFirstName,
                        Strings.isBlank().or(Strings.isLengthLe(6)),
                        value -> String.format("FirstName '%s' must not be null and at least 6 characters long", value)
                )
                .thenMap(Person::getSecondName, ifNotNullAnd(Strings.notBlank(),
                        ofPredicate(Strings.isLengthLe(6), "Minimum second name length is 6")
                ))
                .thenMap(Person::getLastName, Strings::isBlank, "Last name must not be empty")
                .thenMap(
                        Person::getAge,
                        Numbers.<Integer>isNegative().or(Numbers.max(100)),
                        "Age has to be greater than 0 and less than 100"
                )
                .thenMap(Person::getAddresses, eachOf(Validators.<Address>notNull("Address must not be null")
                        .thenMap(Address::getCity, Strings::isBlank, "City must not be blank")
                        .thenMap(Address::getStreet, Strings::isBlank, "Street must not be blank")
                        .thenMap(Address::getBuildingNumber, Numbers::isNegative, "Build number must be positive")
                ))
                .thenMap(Person::getAddresses, eachOf(Validators.<Address>notNull().then(
                        addr -> Strings.isBlank(addr.getCity()) || Strings.isBlank(addr.getStreet()) || Numbers.isNegative(addr.getBuildingNumber()),
                        addr -> String.format("Address of '%s' must be fully filled in", addr)
                )))
                .thenMap(Person::getGender, ifPresent(Strings::isBlank, "Gender must not be blank"));

        Person person1 = new Person("FirstName", "SecondName", "LastName", 76);
        Person person2 = new Person("FirstName", "SecondName", "LastName", 76, address1);
        Person person3 = new Person("FirstName", "SecondName", "LastName", 76, address1, address2);
        Person person4 = new Person("FirstName", "SecondName", "LastName", -76);
        Person person5 = new Person("First", "SecondName", "LastName", 76);
        Person person6 = new Person("FirstName", "", "LastName", 76);
        Person person7 = new Person("FirstName", "sec", "LastName", 76);
        Person person8 = new Person("FirstName", "Second", "", 76);
        Person person9 = new Person("FirstName", "Second", "", 101);
        Person person10 = new Person("FirstName", "Second", "", 10, (Address[]) null);
        Person person11 = new Person();
        Optional<ValidationException> outcome1 = validator.collect(person1);
        Optional<ValidationException> outcome2 = validator.collect(person2);
        Optional<ValidationException> outcome3 = validator.collect(person3);
        Optional<ValidationException> outcome4 = validator.collect(person4);
        Optional<ValidationException> outcome5 = validator.collect(person5);
        Optional<ValidationException> outcome6 = validator.collect(person6);
        Optional<ValidationException> outcome7 = validator.collect(person7);
        Optional<ValidationException> outcome8 = validator.collect(person8);
        Optional<ValidationException> outcome9 = validator.collect(person9);
        Optional<ValidationException> outcome10 = validator.collect(person10);
        Optional<ValidationException> outcome11 = validator.collect(person11);
        assertFalse(outcome1.isPresent());
        assertFalse(outcome2.isPresent());
        assertTrue(outcome3.isPresent());
        assertTrue(outcome4.isPresent());
        assertTrue(outcome5.isPresent());
        assertFalse(outcome6.isPresent());
        assertTrue(outcome7.isPresent());
        assertTrue(outcome8.isPresent());
        assertTrue(outcome9.isPresent());
        assertTrue(outcome10.isPresent());
        assertTrue(outcome11.isPresent());
    }
}

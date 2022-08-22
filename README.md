[![build](https://github.com/buckelieg/validation-fn/workflows/build/badge.svg?branch=master)]()
[![license](https://img.shields.io/github/license/buckelieg/validation-fn.svg)](./LICENSE.md)
[![dist](https://img.shields.io/maven-central/v/com.github.buckelieg/validation-fn.svg)](http://mvnrepository.com/artifact/com.github.buckelieg/validation-fn)
[![javadoc](https://javadoc.io/badge2/com.github.buckelieg/validation-fn/javadoc.svg)](https://javadoc.io/doc/com.github.buckelieg/validation-fn)
# validation-fn
Functional style validation for Java

## Quick reference

Add maven dependency:
```
<dependency>
  <groupId>com.github.buckelieg</groupId>
  <artifactId>validation-fn</artifactId>
  <version>0.1</version>
</dependency>
```
There no transitive dependencies

### Simple validators

```java
Validator<Integer> validator = Validators.<Integer>notNull("Value must not be null")
                                        .then(Predicates.notIn(20, 789, 1001), v -> String.format("Value of '%s' must be one of:  [20, 789, 1001]", v));
// then constructed validator is used to validate an arbitrary values:
Integer value = validator.validate(null); // throws: "Value must not be null"
int value = validator.validate(8); // throws: "Value of '8' must be one of:  [20, 789, 1001]"
```

### Complex validators
Consider we have the next domain model:
```java
public class Address {

    private String city;
    private String street;
    private long buildingNumber;

    public Address(String city, String street, long buildingNumber) {
        this.city = city;
        this.street = street;
        this.buildingNumber = buildingNumber;
    }

    public Address() {
    }

    // getters and setters are dropped for sanity
}
public class Person {

    private String firstName;
    private String secondName;
    private String lastName;
    private int age;
    private List<Address> addresses;
    private Optional<String> gender;

    public Person(String firstName, String secondName, String lastName, int age, Address... addresses) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.lastName = lastName;
        this.age = age;
        this.addresses = null == addresses ? null : Arrays.asList(addresses);
    }

    public Person() {
    }
    
    // getters and setter are dropped for sanity
}
```
User might have a couple of addresses (or might have none) and gender is optional to specify.
This structure somehow brought to our service (e.g. in Data Transfer Object or smth) and it is needed to validated.
Here we have some optional validation paths which must be executed only if corresponding data exists.
Therefore, if validated person has no address - we skip those validation checks for address, but
if some address is present - we must check it for correctness.
Let's take a look to a code which will show us how it would be done using this library:
```java
// Our potential addresses
Address address1 = new Address("MyCity", "MyStreet", 13);
Address address2 = new Address();
// test persons
Person person1 = new Person("FirstName", "SecondName", "LastName", 76);
Person person2 = new Person("FirstName", "SecondName", "LastName", 76, address1);
Person person3 = new Person("FirstName", "SecondName", "LastName", 76, address1, address2);
Person person4 = new Person("FirstName", "SecondName", "LastName", -76);
Person person5 = new Person("First", "SecondName", "LastName", 76);
```
Ok, now we ready to write our validator for those test data:
```java
Validator<Person> validator = Validators.<Person>notNull("Person must be provided")
                .thenMap( // unconditionally validating person object field of 'firstName'
                        Person::getFirstName,
                        Predicates.of(Strings::isBlank).or(Strings.minLength(6)), // validation case in the form of java.util.Predicate 
                        value -> String.format("FirstName '%s' must not be null and at least 6 characters long", value) // error message provider function - the ValidationException message
                )
                .thenMap(
                        Person::getSecondName, // validating person object field of 'secondName'
                        Validator.<String>of().thenIf(
                                Predicates.of(Strings::isBlank).negate(), // field validation condition
                                Validator.ofPredicate( // construct validator from:
                                        Strings.minLength(6), // validation test case predicate
                                        "Minimum second name length is 6" // error message if predicate returns TRUE
                                )
                        )
                )
                .thenMap(Person::getLastName, Strings::isBlank, "Last name must not be empty") // unconditionally validating 'lastName'
                .thenMap(
                        Person::getAge, // validating 'age' field
                        Predicates.<Integer>of(Numbers::isNegative).or(Numbers.max(100)), // combine predicates with arbitrary conditions to be validated against
                        "Age has to be greater than 0 and less than 100" // an error message if we fail
                )
                .thenMap(
                        Person::getAddresses, // walidating address collection
                        Validators.eachOf(Validators.<Address>notNull("Address must not be null")
                            .thenMap(Address::getCity, Strings::isBlank, "City must not be blank")
                            .thenMap(Address::getStreet, Strings::isBlank, "Street must not be blank")
                            .thenMap(Address::getBuildingNumber, Numbers::isNegative, "Build number must be positive")
                        )
                )
                /**
                 * We are free to implement our validators as we desire. For example if we want to validate address at once - we might write the validator like this:
                 * 
                 *.thenMap(Person::getAddresses, Validators.eachOf(Validators.<Address>notNull().then(
                 *  addr -> Strings.isBlank(addr.getCity()) || Strings.isBlank(addr.getStreet()) || Numbers.isNegative(addr.getBuildingNumber()),
                 *  addr -> String.format("Address of '%s' must be fully filled in", addr) // if Address.toString() method is implemented fine we obtain reasonable error description
                 *)))
                 * 
                 */
                .thenMap(
                        Person::getGender, // field 'gender' is optional, so we validating it only if the value is present
                        Validators.ifPresent( // construct predicate that validates on existing value (i.e. optional object is not null and not empty) - if it is - undegroing validation is not performed
                              Strings::isBlank, // test redicate 
                              "Gender must not be blank" // error message
                        )
                );
```
At this stage we are ready to validate our data:
```java
validator.validate(person1); // throws nothing
validator.validate(person2); // throws nothing
validator.validate(person3); // throws ValidationException with message of "City must not be blank" since the second address is not filled at all
validator.validate(person4); // throws ValidationException with message of "Age has to be greater than 0 and less than 100" since age is -76
validator.validate(person5); // throws ValidationException with message of "FirstName 'First' must not be null and at least 6 characters long" since it is 5 characters long
```
### Helper classes
There are some helper classes that makes writing code shorter and easier, these are:
+ Validators - shortcut methods to use Validator
+ Predicates - generic purpose predicates
+ Iterables - predicate collection for iterables
+ Strings - predicate collection for strings
+ Dates - predicate collection for dates
+ Numbers - predicate collection for numbers

These are subject to extension. 

### Prerequisites
Java8, Maven.

## License
This project licensed under Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details


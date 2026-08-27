[![build](https://github.com/buckelieg/validation-fn/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/buckelieg/validation-fn/actions/workflows/build.yml)
[![license](https://img.shields.io/github/license/buckelieg/validation-fn.svg)](./LICENSE)
[![dist](https://img.shields.io/maven-central/v/com.github.buckelieg/validation-fn.svg)](https://central.sonatype.com/artifact/com.github.buckelieg/validation-fn)
[![javadoc](https://javadoc.io/badge2/com.github.buckelieg/validation-fn/javadoc.svg)](https://javadoc.io/doc/com.github.buckelieg/validation-fn)

# validation-fn

Functional style validation for Java

## Quick reference

Add the Maven dependency:

```xml
<dependency>
  <groupId>com.github.buckelieg</groupId>
  <artifactId>validation-fn</artifactId>
  <version>0.3</version>
</dependency>
```
The library has no runtime dependencies.

Validation predicates describe invalid states: when a predicate returns `true`, the validator throws a `ValidationException`. This makes validation rules read as a list of failure conditions.

### Simple validators

```java
Validator<Integer> validator = Validators.<Integer>notNull("Value must not be null")
        .then(Predicates.<Integer>notIn(20, 789, 1001),
                value -> String.format("Value '%s' must be one of [20, 789, 1001]", value));

Integer nullValue = validator.validate(null); // throws: "Value must not be null"
int invalidValue = validator.validate(8); // throws: "Value '8' must be one of [20, 789, 1001]"
```

### Complex validators

Consider the following domain model:

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

    // getters and setters omitted
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
    
    // getters and setters omitted
}
```

Addresses and gender are optional. When an address is present, each of its fields must be valid:

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
```java
Validator<Person> validator = Validators.<Person>notNull("Person must be provided")
                .thenMap( // unconditionally validating person object field of 'firstName'
                        Person::getFirstName,
                        Predicates.of(Strings::isBlank).or(Strings.isLengthLt(6)), // validation case in the form of java.util.Predicate
                        value -> String.format("FirstName '%s' must not be null and at least 6 characters long", value) // error message provider function - the ValidationException message
                )
                .thenMap(
                        Person::getSecondName, // validating person object field of 'secondName'
                        Validator.<String>of().thenIf(
                                Predicates.of(Strings::isBlank).negate(), // field validation condition
                                Validator.ofPredicate( // construct validator from:
                                        Strings.isLengthLt(6), // validation test case predicate
                                        "Minimum second name length is 6" // error message if predicate returns TRUE
                                )
                        )
                )
                .thenMap(Person::getLastName, Strings::isBlank, "Last name must not be empty") // unconditionally validating 'lastName'
                .thenMap(
                        Person::getAge, // validating 'age' field
                        Predicates.<Integer>of(Numbers::isNegative).or(Predicates.ge(100)), // combine predicates with arbitrary conditions to be validated against
                        "Age has to be greater than 0 and less than 100" // an error message if we fail
                )
                .thenMap(
                        Person::getAddresses, // validating address collection
                        Validators.ifNotNull(Validators.eachOf(Validators.<Address>notNull("Address must not be null")
                            .thenMap(Address::getCity, Strings::isBlank, "City must not be blank")
                            .thenMap(Address::getStreet, Strings::isBlank, "Street must not be blank")
                            .thenMap(Address::getBuildingNumber, Numbers::isNegative, "Build number must be positive")
                        ))
                )
                .thenMap(
                        Person::getGender, // field 'gender' is optional, so we validating it only if the value is present
                        Validators.ifPresent( // validate only a present Optional value
                              Strings::isBlank, // validation predicate
                              "Gender must not be blank" // error message
                        )
                );
```

The same validator can be reused for multiple values:

```java
validator.validate(person1); // throws nothing
validator.validate(person2); // throws nothing
validator.validate(person3); // throws ValidationException with message of "City must not be blank" since the second address is not filled at all
validator.validate(person4); // throws ValidationException with message of "Age has to be greater than 0 and less than 100" since age is -76
validator.validate(person5); // throws ValidationException with message of "FirstName 'First' must not be null and at least 6 characters long" since it is 5 characters long
```
### Helper classes

- `Validators` — factories and composition helpers for validators.
- `Predicates` — general-purpose predicates.
- `Iterables` — predicates for iterable values.
- `Strings` — string predicates.
- `Numbers` — numeric predicates.
- `Maps` — map predicates.
- `Classes` — class, array, and interface predicates.

### Building

Java 8 or newer and Maven 3.6.3 or newer are required.

```shell
mvn verify
```

## License

This project is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.

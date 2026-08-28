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
  <version>0.4</version>
</dependency>
```
Version 0.4 contains intentional breaking API changes; see [Migrating from 0.3 to 0.4](MIGRATION.md) when upgrading from 0.3.

The library has no runtime dependencies.

Predicates keep their natural Boolean meaning. Use `Validator.require(...)` when `true` means valid, or `Validator.rejectIf(...)` when `true` describes an invalid state. Validator-based composition is available for conditional and null-guarded rules.

### Simple validators

```java
Validator<Integer> validator = Validators.<Integer>notNull("Value must not be null")
        .thenRequire(Predicates.<Integer>in(20, 789, 1001),
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

Validator<Person> validator = Validators.<Person>notNull("Person must be provided")
        .thenMapRequire(
                Person::getFirstName,
                Strings.notBlank().and(Strings.isLengthGe(6)),
                value -> String.format(
                        "FirstName '%s' must not be null and at least 6 characters long",
                        value
                )
        )
        .thenMap(
                Person::getSecondName,
                Validators.ifNotNullAnd(
                        Strings.notBlank(),
                        Validator.require(Strings.isLengthGe(6), "Minimum second name length is 6")
                )
        )
        .thenMapRequire(Person::getLastName, Strings.notBlank(), "Last name must not be empty")
        .thenMapRequire(
                Person::getAge,
                age -> age >= 0 && age < 100,
                "Age has to be greater than or equal to 0 and less than 100"
        )
        .thenMap(
                Person::getAddresses,
                Validators.ifNotNull(Validators.eachOf(
                        Validators.<Address>notNull("Address must not be null")
                                .thenMapRequire(Address::getCity, Strings.notBlank(), "City must not be blank")
                                .thenMapRequire(Address::getStreet, Strings.notBlank(), "Street must not be blank")
                                .thenMapRequire(Address::getBuildingNumber, Predicates.gt(0L), "Build number must be positive")
                ))
        )
        .thenMap(
                Person::getGender,
                Validators.ifPresent(Validator.require(Strings.notBlank(), "Gender must not be blank"))
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

### Accumulating validation

Ordinary chains are fail-fast. Use `Validators.allOf(...)` when every independent rule must run and all validation failures should be returned together:

```java
Validator<Person> validator = Validators.allOf(
        Validator.require(person -> person.getAge() >= 0, "Age must not be negative"),
        Validator.require(person -> person.getFirstName() != null, "First name is required")
);

try {
    validator.validate(person);
} catch (ValidationException exception) {
    System.out.println(exception.getMessages());
}
```

The thrown aggregate has an empty own message; `getExceptions()` exposes the individual failures and `getMessages()` joins their messages in validator declaration order.

### Explicit predicate semantics

Use validity predicates for requirements and failure predicates only when that form reads more naturally:

```java
Validator<String> required = Validator.require(Strings.notBlank(), "Value must not be blank");

Validator<Integer> nonNegative = Validator.rejectIf(Numbers::isNegative, "Value must not be negative");
```

Use `thenRequire(...)` and `thenMapRequire(...)` for validity predicates. Use `thenRejectIf(...)` and `thenMapRejectIf(...)` for failure predicates. For conditional or null-guarded mapping, pass a `Validator.require(...)` or `Validator.rejectIf(...)` instance to the validator-based `thenMapIf(...)` or `thenMapIfNotNull(...)` overload.

The same rule applies to helpers: neutral composition methods such as `eachOf`, `ifPresent`, `ifNotNull`, and `map` accept a ready `Validator`. Context-aware predicate variants are explicitly named `eachRequire`, `eachRejectIf`, `mapRequire`, and `mapRejectIf`. Use `asValidPredicate()` or `asInvalidPredicate()` when adapting a validator to a predicate.

### Helper classes

- `Validators` — factories and composition helpers for validators.
- `Predicates` — general-purpose predicates.
- `Iterables` — predicates for iterable values.
- `Strings` — string predicates.
- `Numbers` — numeric predicates.
- `Maps` — map predicates.
- `Classes` — class, array, and interface predicates.

These helper APIs are non-instantiable `final` utility classes. `Predicates` remains an enum because `TRUE` and `FALSE` are actual enum constants.

### Building

Java 8 or newer and Maven 3.6.3 or newer are required.

```shell
mvn verify
```

The `verify` lifecycle runs the test suite, creates the JaCoCo report under `target/site/jacoco`, and enforces the project's coverage thresholds: 99% instructions, 98% branches, 100% lines, and 100% methods.

## License

This project is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.

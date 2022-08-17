[![build](https://github.com/buckelieg/validation-fn/workflows/build/badge.svg?branch=master)]()
[![license](https://img.shields.io/github/license/buckelieg/validation-fn.svg)](./LICENSE.md)
[![dist](https://img.shields.io/maven-central/v/com.github.buckelieg/validation-fn.svg)](http://mvnrepository.com/artifact/com.github.buckelieg/validation-fn)
[![javadoc](https://javadoc.io/badge2/com.github.buckelieg/validation-fn/javadoc.svg)](https://javadoc.io/doc/com.github.buckelieg/validation-fn)
# validation-fn
Functional style validation for Java

## Quick reference

1) Add maven dependency:
```
<dependency>
  <groupId>com.github.buckelieg</groupId>
  <artifactId>validation-fn</artifactId>
  <version>0.1</version>
</dependency>
```
### Simple validators

```java
    Validator<Number> validator = Validator.<Number>notNull("Value must not be null")
                                           .then(Numbers::isNumber, "Value must be a number")
                                           .thenMap(
                                                   BigDecimal::new,
                                                   Predicates::isNegative, 
                                                   "Value must not be negaative"
                                            )
                                           .then(
                                                   Predicates.notIn(-20, -789, -1001), 
                                                   v -> String.format("Value of '%s' is not in the list of:  [-20, -789, -1001]", v)
                                           );
// then constructed validator is used to validate an erbitrary values:
validator.validate(null); // throws first case
validator.validate(8); // throws third case
```

### Complex validators
```java
// TODO
```

### Prerequisites
Java8, Maven, Appropriate JDBC driver.

## License
This project licensed under Apache License, Version 2.0 - see the [LICENSE.md](LICENSE.md) file for details


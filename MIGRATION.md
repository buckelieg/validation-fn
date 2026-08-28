# Migrating from 0.3 to 0.4

Version 0.4 removes the deprecated predicate-based API and makes predicate direction explicit. It is source- and binary-incompatible with 0.3, so applications must be recompiled after migration.

## Predicate direction

Use `require` when a predicate describes a valid value:

```java
Validator<String> validator = Validator.require(
        Strings.notBlank(),
        "Value must not be blank"
);
```

Use `rejectIf` when a predicate describes an invalid value:

```java
Validator<Integer> validator = Validator.rejectIf(
        Numbers::isNegative,
        "Value must not be negative"
);
```

## API replacements

All message variants have the same replacement, including `String`, `Function`, and `BiFunction` overloads.

| Removed in 0.4 | Replacement when `true` means invalid |
| --- | --- |
| `Validator.ofPredicate(predicate, message)` | `Validator.rejectIf(predicate, message)` |
| `validator.then(predicate, message)` | `validator.thenRejectIf(predicate, message)` |
| `validator.thenIfNotNull(predicate, message)` | `validator.thenIfNotNull(Validator.rejectIf(predicate, message))` |
| `validator.thenMap(mapper, predicate, message)` | `validator.thenMapRejectIf(mapper, predicate, message)` |
| `validator.thenMapIf(condition, mapper, predicate, message)` | `validator.thenMapIf(condition, mapper, Validator.rejectIf(predicate, message))` |
| `validator.thenMapIfNotNull(mapper, predicate, message)` | `validator.thenMapIfNotNull(mapper, Validator.rejectIf(predicate, message))` |

When the predicate naturally describes validity, remove the negation and use the requirement form:

| 0.3 pattern | Preferred 0.4 form |
| --- | --- |
| `Validator.ofPredicate(valid.negate(), message)` | `Validator.require(valid, message)` |
| `validator.then(valid.negate(), message)` | `validator.thenRequire(valid, message)` |
| `validator.thenMap(mapper, valid.negate(), message)` | `validator.thenMapRequire(mapper, valid, message)` |

The predicate overloads in `Validators` were also removed. Neutral helpers now accept a ready validator, while context-aware predicates use an explicit direction:

| Removed in 0.4 | Replacement |
| --- | --- |
| `Validators.eachOf(predicate, message)` | `Validators.eachOf(Validator.rejectIf(predicate, message))` |
| `Validators.eachOf(contextPredicate, message)` | `Validators.eachRejectIf(contextPredicate, message)` |
| `Validators.ifPresent(predicate, message)` | `Validators.ifPresent(Validator.rejectIf(predicate, message))` |
| `Validators.ifNotNull(predicate, message)` | `Validators.ifNotNull(Validator.rejectIf(predicate, message))` |
| `Validators.ifNotNullAnd(condition, predicate, message)` | `Validators.ifNotNullAnd(condition, Validator.rejectIf(predicate, message))` |
| `Validators.map(mapper, predicate, message)` | `Validators.mapRejectIf(mapper, predicate, message)` |
| `Validators.notNullOr(predicate, message)` | `Validator.require(value -> value != null && !predicate.test(value), message)` |

For validity predicates, use `Validator.require`, `Validators.eachRequire`, or `Validators.mapRequire` instead of negating the predicate.

## Predicate adapters

`Validator.toPredicate()` was removed because its direction was not apparent from its name. Choose the required Boolean meaning explicitly:

```java
Predicate<T> valid = validator.asValidPredicate();
Predicate<T> invalid = validator.asInvalidPredicate();
```

For conditional and null-guarded mapping, pass an explicitly constructed validator:

```java
Validator<Person> validator = Validator.<Person>of()
        .thenMapIf(
                Person::hasName,
                Person::getName,
                Validator.require(Strings.notBlank(), "Name must not be blank")
        )
        .thenMapIfNotNull(
                Person::getAddress,
                Validator.require(Address::isValid, "Address is invalid")
        );
```

## Null argument checks

All constant-message overloads now reject a `null` message when the validator is created. Code that previously failed later during `validate()` now fails immediately with `NullPointerException`:

```java
Validators.notNull((String) null); // fails immediately in 0.4
```

## Utility containers and accumulating validation

The empty utility enums `Classes`, `Iterables`, `Maps`, `Numbers`, `Ranges`, `Strings`, and `Validators` are now non-instantiable `final` classes. Normal static method calls are unchanged, but enum-specific reflection and the generated `values()` and `valueOf()` methods are no longer available. `Predicates` remains an enum because it defines the real constants `TRUE` and `FALSE`.

`Validators.allOf(...)` is new in 0.4. Unlike ordinary fail-fast chains, it executes every supplied validator and throws a `ValidationException` containing all validation failures:

```java
Validator<T> validator = Validators.allOf(firstRule, secondRule, thirdRule);
```

## Migration checklist

1. Replace every removed method using the tables above.
2. Prefer `require` for ordinary validity predicates and `rejectIf` only for predicates that describe failures.
3. Recompile all consumers; binaries compiled against 0.3 are not compatible with 0.4.
4. Run the full test suite and verify both passing and failing values for every migrated rule.

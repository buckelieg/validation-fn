# Changelog

All notable changes to this project are documented in this file.

## 0.4 - 2026-08-28

### Added

- Explicit validity and failure factories: `require` and `rejectIf`.
- Explicit sequential and mapped composition through `thenRequire`, `thenRejectIf`, `thenMapRequire`, and `thenMapRejectIf`.
- Explicit validator adapters: `asValidPredicate` and `asInvalidPredicate`.
- Accumulating validation through `Validators.allOf`, including ordered nested failures in `ValidationException`.
- A migration guide for applications upgrading from 0.3.
- JaCoCo reports and mandatory coverage checks in the Maven `verify` lifecycle.
- Contract tests for composition, null handling, aggregation, utility containers, and predicate direction.

### Changed

- Predicate direction is now visible from every public method name: `true` means valid for `*Require` and invalid for `*RejectIf`.
- Constant validation messages are checked for `null` when validators are created.
- Empty utility enums are now non-instantiable `final` utility classes.
- Javadocs now use strict `doclint=all` during release builds.
- The public API documentation and examples now use only explicit predicate semantics.

### Removed

- Deprecated `Validator.ofPredicate` and ambiguous predicate-based composition overloads.
- Ambiguous predicate overloads from `Validators.eachOf`, `ifPresent`, `ifNotNull`, `ifNotNullAnd`, and `map`.
- `Validator.toPredicate`; use `asValidPredicate` or `asInvalidPredicate` instead.
- `Validators.notNullOr`; express the validity requirement directly with `Validator.require`.

### Compatibility

- Version 0.4 is source- and binary-incompatible with 0.3. See [MIGRATION.md](MIGRATION.md) for direct replacements and examples.

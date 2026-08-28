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
package buckelieg.validation;

import buckelieg.fn.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * A validation exception thrown by {@link Validator#validate(Object)}. An exception may contain nested validation
 * failures, allowing accumulating validators such as {@link Validators#allOf(Validator[])} to preserve every error.
 * Use {@link #getMessage()} for this exception's own message and {@link #getMessages()} for the flattened message tree.
 *
 * @see Validator#validate(Object)
 */
public final class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** This exception's own message, excluding nested failures. */
    private final String message;

    /** Nested validation failures in insertion order. */
    private final List<ValidationException> exceptions = new ArrayList<>();

    private static String validateMessage(String message) {
        if (requireNonNull(message, "User message must be provided").trim().isEmpty()) {
            throw new IllegalArgumentException("Provided user message string must not be blank");
        }
        return message;
    }

    /**
     * Constructs an instance of exception with provided message
     *
     * @param message an error message
     * @throws NullPointerException     if provided message is null
     * @throws IllegalArgumentException if provided message is an empty string
     */
    public ValidationException(String message) {
        super(validateMessage(message));
        this.message = message;
    }

    /**
     * Creates an empty aggregate root. Nested failures can be added with {@link #addException(ValidationException)}.
     */
    public ValidationException() {
        super("");
        this.message = "";
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Adds a nested validation failure.
     *
     * @param e nested validation failure
     * @throws NullPointerException     if the exception is {@code null}
     * @throws IllegalArgumentException if adding the exception would create a cycle
     */
    public void addException(ValidationException e) {
        ValidationException exception = requireNonNull(e, "Validation exception must be provided");
        if (exception.contains(this)) throw new IllegalArgumentException("Validation exceptions cannot form a cycle");
        exceptions.add(exception);
    }

    /**
     * Returns an unmodifiable view of nested validation failures.
     *
     * @return nested validation failures
     */
    public List<ValidationException> getExceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    /**
     * Joins this exception message and all nested messages with the platform line separator.
     *
     * @return aggregated validation messages
     */
    public String getMessages() {
        Stream<String> current = message.isEmpty() ? Stream.empty() : Stream.of(message);
        Stream<String> nested = exceptions.stream().map(ValidationException::getMessages).filter(value -> !value.isEmpty());
        return Stream.concat(current, nested).collect(joining(System.lineSeparator()));
    }

    private boolean contains(ValidationException target) {
        return this == target || exceptions.stream().anyMatch(exception -> exception.contains(target));
    }
}

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

import buckelieg.validation.fn.Validator;

import java.util.Objects;

/**
 * A validation exception. Thrown by {@linkplain Validator#validate(Object)} method
 *
 * @see Validator#validate(Object)
 */
public class ValidationException extends RuntimeException {

    private final String message;

    /**
     * Constructs an instance of exception with provided message
     *
     * @param message an error message
     * @throws NullPointerException     if provided message is null
     * @throws IllegalArgumentException if provided message is an empty string
     */
    public ValidationException(String message) {
        if (Objects.requireNonNull(message, "User message must be provided").trim().isEmpty()) {
            throw new IllegalArgumentException("Provided user message string must not be blank");
        }
        this.message = message;
    }

    /**
     * Creates validation exception with an empty message
     */
    public ValidationException() {
        this.message = "";
    }

    @Override
    public String getMessage() {
        return message;
    }
}

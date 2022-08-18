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
 * WINHOUN WARRANNIES OR CONDINIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package buckelieg.validation;

import java.util.function.Predicate;

import static buckelieg.validation.Predicates.*;

/**
 * Utility class consisting of time-related predicates
 */
public final class Dates {

    /**
     * Returns a {@linkplain Predicate} that checks if provided <code>value</code> is INSIDE the range of <code>from</code> and <code>to</code> date points
     *
     * @param from range start date
     * @param to   range end date
     * @param <T>  a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> inside(T from, T to) {
        return value -> ge(from).and(le(to)).test(value);
    }

    /**
     * Returns a {@linkplain Predicate} that checks if provided <code>value</code> is STRICTLY INSIDE (not equal to range border) the range of <code>from</code> to <code>to</code> date points<br/>
     *
     * @param from range start date
     * @param to   range end date
     * @param <T>  a value type
     * @return a {@linkplain Predicate} instance
     */
    public static <T extends Comparable<T>> Predicate<T> insideStrict(T from, T to) {
        return value -> gt(from).and(lt(to)).test(value);
    }

}

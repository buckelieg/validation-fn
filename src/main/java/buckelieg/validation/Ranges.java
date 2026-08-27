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

import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Utility class consisting of range-related predicates
 */
public enum Ranges {

  ;

  private static <T extends Number & Comparable<T>> void requireValidRange(T from, T to) {
	requireNonNull(from, "Range start must be provided");
	requireNonNull(to, "Range end must be provided");
	if (from.compareTo(to) > 0) throw new IllegalArgumentException("Range start must not be greater than range end");
  }

  /**
   * Returns a {@linkplain Predicate} that checks if provided <code>value</code> is INSIDE the range of <code>from</code> and <code>to</code> points
   *
   * @param from range start
   * @param to   range end
   * @param <T>  a value type
   * @return a {@linkplain Predicate} instance
   */
  public static <T extends Number & Comparable<T>> Predicate<T> inside(T from, T to) {
	requireValidRange(from, to);
	return Predicates.ge(from).and(Predicates.le(to));
  }

  /**
   * Returns a {@linkplain Predicate} that checks if provided <code>value</code> is OUTSIDE the range of <code>from</code> and <code>to</code> points
   *
   * @param from range start
   * @param to   range end
   * @param <T>  a value type
   * @return a {@linkplain Predicate} instance
   */
  public static <T extends Number & Comparable<T>> Predicate<T> outside(T from, T to) {
	requireValidRange(from, to);
	return Predicates.lt(from).or(Predicates.gt(to));
  }

  /**
   * Returns a {@linkplain Predicate} that checks if provided <code>value</code> is STRICTLY INSIDE (not equal to range border) the range of <code>from</code> to <code>to</code> points<br/>
   *
   * @param from range start
   * @param to   range end
   * @param <T>  a value type
   * @return a {@linkplain Predicate} instance
   */
  public static <T extends Number & Comparable<T>> Predicate<T> strictInside(T from, T to) {
	requireValidRange(from, to);
	return Predicates.gt(from).and(Predicates.lt(to));
  }

}

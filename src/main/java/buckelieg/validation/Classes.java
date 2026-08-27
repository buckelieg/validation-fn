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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * A collection of class and reflection-related predicates
 */
public enum Classes {

  ;

  /**
   * Test if provided value type (class) belongs to particular one
   *
   * @param clazz a target type of the value to test against
   * @param <T>   value type
   * @return a {@linkplain Predicate} instance
   * @see Class#isAssignableFrom(Class)
   */
  public static <T> Predicate<T> isOfType(Class<T> clazz) {
	requireNonNull(clazz, "Class must be provided");
	return value -> clazz.isAssignableFrom(value.getClass());
  }

  /**
   * Test if provided value type (class) belongs to particular one
   *
   * @param clazz a target type of the value to test against
   * @param <T>   value type
   * @return a {@linkplain Predicate} instance
   */
  public static <T> Predicate<T> isExact(Class<T> clazz) {
	requireNonNull(clazz, "Class must be provided");
	return value -> Objects.equals(value.getClass(), clazz);
  }

  /**
   * @param clazz a target type of the value to test against
   * @param <T>   value type
   * @return a {@linkplain Predicate} instance
   * @see Class#isInstance(Object)
   */
  public static <T> Predicate<T> isInstanceOf(Class<T> clazz) {
	requireNonNull(clazz, "Class must be provided");
	return clazz::isInstance;
  }

  /**
   *
   * @param value
   * @param <T> value type
   * @return a {@linkplain Predicate} instance
   * @see Class#isArray()
   */
  public static <T> boolean isArray(T value) {
	return value.getClass().isArray();
  }

  /**
   * @param <T> value type
   * @return a {@linkplain Predicate} instance
   */
  public static <T> Predicate<T> isArray() {
	return Classes::isArray;
  }

  /**
   *
   * @param clazz
   * @param <T> value type
   * @return a {@linkplain Predicate} instance
   */
  public static <T> Predicate<T> isArrayOfType(Class<T> clazz) {
	requireNonNull(clazz, "Class must be provided");
	return value -> {
	  Class<?> valueClass = value.getClass();
	  return valueClass.isArray() && valueClass.getComponentType().isAssignableFrom(clazz);
	};
  }

  /**
   *
   * @param value
   * @param <T> value type
   * @return a {@linkplain Predicate} instance
   * @see Class#isPrimitive()
   */
  public static <T> boolean isPrimitive(T value) {
	return value.getClass().isPrimitive();
  }

  /**
   *
   * @return a {@linkplain Predicate} instance
   * @param <T> value type
   */
  public static <T> Predicate<T> isPrimitive() {
	return Classes::isPrimitive;
  }

  /**
   *
   * @param value
   * @param <T> value type
   * @return a {@linkplain Predicate} instance
   * @see Class#isInterface()
   */
  public static <T> boolean isInterface(T value) {
	return value.getClass().isInterface();
  }

  /**
   *
   * @param <T> value type
   * @return a {@linkplain Predicate} instance
   */
  public static <T> Predicate<T> isInterface() {
	return Classes::isInterface;
  }

  /**
   *
   * @param clazz
   * @return
   * @param <T> value type
   */
  public static <T> Predicate<T> hasInterface(Class<?> clazz) {
	requireNonNull(clazz, "Class must be provided");
	return value -> Utils.allInterfaces(value).stream().anyMatch(clazz::isAssignableFrom);
  }

  /**
   *
   * @param classes
   * @return
   * @param <T> value type
   */
  public static <T> Predicate<T> implementsAll(Class<?>... classes) {
	return value -> {
	  List<Class<?>> interfaces = Utils.allInterfaces(value);
	  List<Class<?>> list = Arrays.stream(classes).filter(Class::isInterface).collect(Collectors.toList());
	  return new HashSet<>(interfaces).containsAll(list);
	};
  }

  /**
   *
   * @param classes
   * @return
   * @param <T> value type
   */
  public static <T> Predicate<T> implementsAllExact(Class<?>... classes) {
	return null;
  }

  /**
   *
   * @param classes
   * @return
   * @param <T> value type
   */
  public static <T> Predicate<T> implementsAny(Class<?>... classes) {
	return value -> {
	  List<Class<?>> interfaces = Utils.allInterfaces(value);
	  List<Class<?>> list = Arrays.stream(classes).filter(Class::isInterface).collect(Collectors.toList());
	  return new HashSet<>(interfaces).containsAll(list);
	};
  }

}

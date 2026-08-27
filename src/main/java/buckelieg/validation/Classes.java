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

  private static Class<?> valueType(Object value) {
	requireNonNull(value, "Value must be provided");
	return value instanceof Class<?> ? (Class<?>) value : value.getClass();
  }

  private static List<Class<?>> interfaceTypes(Class<?>... classes) {
	requireNonNull(classes, "Interface types must be provided");
	return Arrays.stream(classes).map(clazz -> {
	  requireNonNull(clazz, "Interface type must be provided");
	  if (!clazz.isInterface()) {
		throw new IllegalArgumentException(clazz.getName() + " is not an interface");
	  }
	  return clazz;
    }).collect(Collectors.toList());
  }

  /**
   * Checks whether a value is an instance of the provided type.
   *
   * @param clazz a target type of the value to test against
   * @param <T>   value type
   * @return a {@linkplain Predicate} instance
   * @see Class#isInstance(Object)
   */
  public static <T> Predicate<T> isOfType(Class<T> clazz) {
	requireNonNull(clazz, "Class must be provided");
	return clazz::isInstance;
  }

  /**
   * Checks whether a value has exactly the provided runtime type.
   *
   * @param clazz a target type of the value to test against
   * @param <T>   value type
   * @return a {@linkplain Predicate} instance
   */
  public static <T> Predicate<T> isExact(Class<T> clazz) {
	requireNonNull(clazz, "Class must be provided");
	return value -> null != value && Objects.equals(valueType(value), clazz);
  }

  /**
   * Checks whether a value is an instance of the provided type.
   *
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
   * Checks whether a value or class token represents an array type.
   *
   * @param value a value or class token
   * @param <T> value type
   * @return true if the represented type is an array
   * @see Class#isArray()
   */
  public static <T> boolean isArray(T value) {
	return null != value && valueType(value).isArray();
  }

  /**
   * Returns a predicate that checks whether a value or class token represents an array type.
   *
   * @param <T> value type
   * @return a {@linkplain Predicate} instance
   */
  public static <T> Predicate<T> isArray() {
	return Classes::isArray;
  }

  /**
   * Returns a predicate that checks whether an array component is assignable to the provided type.
   *
   * @param clazz required array component type
   * @return a {@linkplain Predicate} instance
   */
  public static Predicate<Object> isArrayOfType(Class<?> clazz) {
	requireNonNull(clazz, "Class must be provided");
	return value -> {
	  if (null == value) return false;
	  Class<?> valueClass = valueType(value);
	  return valueClass.isArray() && clazz.isAssignableFrom(valueClass.getComponentType());
	};
  }

  /**
   * Checks whether a value or class token represents a primitive type.
   *
   * @param value a value or class token
   * @param <T> value type
   * @return a {@linkplain Predicate} instance
   * @see Class#isPrimitive()
   */
  public static <T> boolean isPrimitive(T value) {
	return null != value && valueType(value).isPrimitive();
  }

  /**
   * Returns a predicate that checks whether a value or class token represents a primitive type.
   *
   * @return a {@linkplain Predicate} instance
   * @param <T> value type
   */
  public static <T> Predicate<T> isPrimitive() {
	return Classes::isPrimitive;
  }

  /**
   * Checks whether a value or class token represents an interface.
   *
   * @param value a value or class token
   * @param <T> value type
   * @return a {@linkplain Predicate} instance
   * @see Class#isInterface()
   */
  public static <T> boolean isInterface(T value) {
	return null != value && valueType(value).isInterface();
  }

  /**
   * Returns a predicate that checks whether a value or class token represents an interface.
   *
   * @param <T> value type
   * @return a {@linkplain Predicate} instance
   */
  public static <T> Predicate<T> isInterface() {
	return Classes::isInterface;
  }

  /**
   * Checks whether a value implements the provided interface or one of its subinterfaces.
   *
   * @param clazz required interface
   * @return a {@linkplain Predicate} instance
   * @param <T> value type
   */
  public static <T> Predicate<T> hasInterface(Class<?> clazz) {
	requireNonNull(clazz, "Class must be provided");
	if (!clazz.isInterface()) throw new IllegalArgumentException(clazz.getName() + " is not an interface");
	return value -> null != value && Utils.allInterfaces(value).stream().anyMatch(clazz::isAssignableFrom);
  }

  /**
   * Checks whether a value implements every provided interface, including through subinterfaces.
   *
   * @param classes required interfaces
   * @return a {@linkplain Predicate} instance
   * @param <T> value type
   */
  public static <T> Predicate<T> implementsAll(Class<?>... classes) {
	List<Class<?>> required = interfaceTypes(classes);
	return value -> {
	  if (null == value) return false;
	  List<Class<?>> implemented = Utils.allInterfaces(value);
	  return required.stream().allMatch(
		  requiredType -> implemented.stream().anyMatch(requiredType::isAssignableFrom)
	  );
	};
  }

  /**
   * Checks whether a value implements every provided interface by exact interface type.
   *
   * @param classes required interfaces
   * @return a {@linkplain Predicate} instance
   * @param <T> value type
   */
  public static <T> Predicate<T> implementsAllExact(Class<?>... classes) {
	List<Class<?>> required = interfaceTypes(classes);
	return value -> null != value && new HashSet<>(Utils.allInterfaces(value)).containsAll(required);
  }

  /**
   * Checks whether a value implements at least one provided interface, including through subinterfaces.
   *
   * @param classes candidate interfaces
   * @return a {@linkplain Predicate} instance
   * @param <T> value type
   */
  public static <T> Predicate<T> implementsAny(Class<?>... classes) {
	List<Class<?>> required = interfaceTypes(classes);
	return value -> {
	  if (null == value) return false;
	  List<Class<?>> implemented = Utils.allInterfaces(value);
	  return required.stream().anyMatch(
		  requiredType -> implemented.stream().anyMatch(requiredType::isAssignableFrom)
	  );
	};
  }

}

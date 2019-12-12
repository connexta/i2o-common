/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package com.connexta.commons.function;

import java.util.function.Function;

/**
 * The <code>ThrowingFunction</code> interface expands on the {@link java.util.function.Function}
 * interface to provide the ability to throw back exceptions.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of exceptions that can be thrown by the operation
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
  /**
   * Applies this function to the given argument.
   *
   * @param t the function argument
   * @return the function result
   * @throws E if an error occurs
   */
  public R apply(T t) throws E;

  /**
   * Provides a version of this function that will limit all exceptions it throws to the specified
   * subclass; wrapping all others as required as instances of the specified class.
   *
   * <p><i>Note:</i> All runtime exceptions will bubble out unless they are instances of the
   * specified class.
   *
   * @param <S> a sub type of exception <code>E</code>
   * @param clazz the subclass of exceptions to limit the function to throw
   * @param creator a creator function to create an instance of the specified exception from the
   *     specified one
   * @return a version of this function that can be used as a field function
   */
  public default <S extends E> ThrowingFunction<T, R, S> limitedTo(
      Class<S> clazz, Function<Exception, S> creator) {
    return t -> {
      try {
        return apply(t);
      } catch (Exception e) {
        if (clazz.isInstance(e)) {
          throw clazz.cast(e);
        } else if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        } else {
          throw creator.apply(e);
        }
      }
    };
  }

  /**
   * Returns a function that always returns its input argument.
   *
   * @param <T> the type of the input and output objects to the function
   * @param <E> the type of exceptions that can be thrown by the operation
   * @return a function that always returns its input argument
   */
  public static <T, E extends Exception> ThrowingFunction<T, T, E> identity() {
    return t -> t;
  }
}

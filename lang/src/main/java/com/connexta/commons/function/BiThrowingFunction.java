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
 * The <code>BiThrowingFunction</code> interface expands on the {@link Function} interface to
 * provide the ability to throw back exceptions.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of exceptions that can be thrown by the operation
 * @param <F> another type of exceptions that can be thrown by the operation
 */
@FunctionalInterface
public interface BiThrowingFunction<T, R, E extends Exception, F extends Exception> {
  /**
   * Applies this function to the given argument.
   *
   * @param t the function argument
   * @return the function result
   * @throws E if an error occurs
   * @throws F if an error occurs
   */
  public R apply(T t) throws E, F;

  /**
   * Returns a function that always returns its input argument.
   *
   * @param <T> the type of the input and output objects to the function
   * @param <E> the type of exceptions that can be thrown by the operation
   * @param <F> another type of exceptions that can be thrown by the operation
   * @return a function that always returns its input argument
   */
  public static <T, E extends Exception, F extends Exception>
      BiThrowingFunction<T, T, E, F> identity() {
    return t -> t;
  }
}

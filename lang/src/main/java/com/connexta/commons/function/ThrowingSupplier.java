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

/**
 * The <code>ThrowingSupplier</code> interface expands on the {@link java.util.function.Supplier}
 * interface to provide the ability to throw back exceptions.
 *
 * @param <R> the type of results supplied by the supplier
 * @param <E> the type of exceptions that can be thrown by the operation
 */
@FunctionalInterface
public interface ThrowingSupplier<R, E extends Exception> {
  /**
   * Performs this operation on the given arguments.
   *
   * @return a result
   * @throws E if an error occurs
   */
  public R get() throws E;
}

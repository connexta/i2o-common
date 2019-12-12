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
 * The <code>BiThrowingRunnable</code> interface expands on the {@link Runnable} interface to
 * provide the ability to throw back exceptions.
 *
 * @param <E> the type of exceptions that can be thrown by the command
 * @param <F> another type of exceptions that can be thrown by the command
 */
@FunctionalInterface
public interface BiThrowingRunnable<E extends Exception, F extends Exception> {
  /**
   * Executes user-defined code.
   *
   * @throws E if an error occurs
   * @throws F if an error occurs
   */
  public void run() throws E, F;
}

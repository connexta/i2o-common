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
package com.connexta.commons.queue;

/** Interface used for all error codes enumerations that can be generated by tasks. */
public interface ErrorCode {
  /**
   * Checks whether or not a task that fails because of this error should be re-processed later in a
   * re-attempt to complete it.
   *
   * @return <code>true</code> if a task that fails because of this error should be retried; <code>
   * false</code> if not
   */
  public boolean isRetryable();

  /**
   * Checks if this error code represents an unknown error code that is used for forward
   * compatibility where the current code might not be able to understand a new error code that it
   * read and would map this new error code to <code>UNKNOWN</code>. Then it would most likely
   * ignore it.
   *
   * @return <code>true</code> if this error code represents an unknown error code
   */
  public boolean isUnknown();
}

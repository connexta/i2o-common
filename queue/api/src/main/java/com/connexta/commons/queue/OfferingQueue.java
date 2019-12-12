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

import java.util.concurrent.TimeUnit;

/**
 * Interface for a queue that allows tasks to be inserted/offered in addition to be retrieved.
 *
 * @param <E> the base exception class for all exceptions that can be thrown from this API
 * @param <C> the enumeration for all error codes specified by this API
 * @param <I> the type of information associated with a task
 * @param <T> the type of task that can be retrieved from this queue
 */
public interface OfferingQueue<
        E extends Exception, C extends ErrorCode, I extends TaskInfo, T extends Task<E, C>>
    extends Queue<E, C, T> {
  /**
   * Gets the unique identifier for this queue.
   *
   * @return the queue id
   */
  public Id getId();

  /**
   * Inserts a task with the specified information with the given priority into this queue, waiting
   * if necessary for space to become available.
   *
   * @param info the info for the task to be added to the queue
   * @throws InterruptedException if the current thread was interrupted while waiting to insert the
   *     task
   * @throws E if an error occurred while performing the operation
   */
  public void put(I info) throws InterruptedException, E;

  /**
   * Inserts a task for the specified information and with the given priority into this queue if it
   * is possible to do so immediately without violating capacity restrictions.
   *
   * @param info the info for the task to be added to the queue
   * @return <code>true</code> if the task was added to this queue; <code>false</code> otherwise
   * @throws E if an error occurred while performing the operation
   */
  public boolean offer(I info) throws E;

  /**
   * Inserts a task for the specified information and with the given priority into this queue,
   * waiting up to the specified maximum amount of time if necessary for space to become available.
   *
   * @param info the info for the task to be added to the queue
   * @param timeout how long to wait before giving up, in units of <code>unit</code>
   * @param unit a {@code TimeUnit} determining how to interpret the <code>timeout</code> parameter
   * @return <code>true</code> if the task was added to this queue; <code>false</code> otherwise
   * @throws InterruptedException if the current thread was interrupted while waiting to insert the
   *     task
   * @throws E if an error occurred while performing the operation
   */
  public boolean offer(I info, long timeout, TimeUnit unit) throws InterruptedException, E;
}

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

import com.connexta.commons.queue.Queue.Id;
import java.util.stream.Stream;

/**
 * A queue broker defines the point of entry for retrieving references to queues and for monitoring
 * tasks to be processed.
 *
 * @param <E> the base exception class for all exceptions that can be thrown from this API
 * @param <C> the enumeration for all error codes specified by this API
 * @param <I> the base type of information associated with a task
 * @param <O> the base type of all offering queues managed by this broker
 * @param <Q> the base type for all queues managed by this broker
 * @param <S> the base type for all composite queues managed by this broker
 * @param <T> the base type for all tasks managed by the queues
 */
public interface QueueBroker<
    E extends Exception,
    C extends ErrorCode,
    I extends TaskInfo,
    O extends OfferingQueue<E, C, I, T>,
    Q extends Queue<E, C, T>,
    S extends CompositeQueue<E, C, I, O, T>,
    T extends Task<E, C>> {
  /**
   * Gets a queue where tasks will be queued.
   *
   * <p><i>Note:</i> New queues should be deployed if none currently exist for a given identifier.
   *
   * @param id the id for the queue to retrieve
   * @return the corresponding queue
   * @throws IllegalArgumentException if this broker doesn't support queues for the specified class
   *     of task
   * @throws E if an error occurs while retrieving the queue
   */
  public O getQueue(Id id) throws E;

  /**
   * Gets a queue representing a specific set of queues. Polling from the returned queue will
   * attempt to find the first available task based on priority level from any of the specified
   * queues. There is no priority established between queues.
   *
   * <p><i>Note:</i> New queues should be deployed if none currently exist for a given identifier.
   *
   * @param ids the ids for the queues to compound together into one virtual queue
   * @return a queue representing all specified queues (this might or might not be an {@link
   *     OfferingQueue} or <code>O</code> if only one id was specified; otherwise it would be a
   *     {@link CompositeQueue} or <code>S</code>)
   * @throws IllegalArgumentException if this broker doesn't support queues for the specified class
   *     of task
   * @throws E if an error occurred while performing the operation
   */
  @SuppressWarnings({
    "squid:S1905" /* case is unfortunately required as generics cannot have multiple upper bounds */
  })
  public default Q getQueue(Id... ids) throws E {
    return (ids.length == 1) ? (Q) getQueue(ids[0]) : (Q) getQueue(Stream.of(ids));
  }

  /**
   * Gets a queue representing a specific set of queues. Polling from the returned queue will
   * attempt to find the first available task based on priority level from any of the specified
   * queues. There is no priority established between queues.
   *
   * <p><i>Note:</i> New queues should be deployed if none currently exist for a given identifier.
   *
   * @param ids the ids for the queues to compound together into one virtual queue
   * @return a composite queue representing all specified queues
   * @throws IllegalArgumentException if this broker doesn't support queues for the specified class
   *     of task
   * @throws E if an error occurred while performing the operation
   */
  public S getQueue(Stream<Id> ids) throws E;
}

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

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface for all type of composite queues from which tasks can be retrieved. A composite queue
 * is an artifact created around multiple offering queues allowing a worker to retrieve tasks from
 * any of the queues based on task priorities.
 *
 * @param <E> the base exception class for all exceptions that can be thrown from this API
 * @param <C> the enumeration for all error codes specified by this API
 * @param <I> the type of information associated with a task
 * @param <O> the type of queues managed by this composite queue
 * @param <T> the type of task that can be retrieved from this queue
 */
public interface CompositeQueue<
        E extends Exception,
        C extends ErrorCode,
        I extends TaskInfo,
        O extends OfferingQueue<E, C, I, T>,
        T extends Task<E, C>>
    extends Queue<E, C, T> {
  /**
   * Gets all offering queues that are compounded together.
   *
   * @return a stream of all offering queues compounded together
   */
  public Stream<O> queues();

  /**
   * Gets all queue identifiers that are compounded together.
   *
   * @return a stream of all queue ids that are compounded together
   */
  public default Stream<Id> ids() {
    return queues().map(OfferingQueue::getId);
  }

  /**
   * Gets an offering queue from this composite queue given its identifier.
   *
   * @param id the id of the offering queue to retrieve
   * @return the corresponding offering queue or empty if no queue was compounded with the specified
   *     id
   */
  public default Optional<O> getQueue(Id id) {
    return queues().filter(q -> q.getId().equals(id)).findFirst();
  }
}

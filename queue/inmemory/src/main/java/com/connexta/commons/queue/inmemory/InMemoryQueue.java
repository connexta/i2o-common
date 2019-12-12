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
package com.connexta.commons.queue.inmemory;

import com.connexta.commons.queue.ErrorCode;
import com.connexta.commons.queue.Queue;
import com.connexta.commons.queue.Task.State;
import com.connexta.commons.queue.TaskInfo;
import com.connexta.commons.queue.impl.persistence.AbstractTaskPojo;

/**
 * Base interface for all type of in-memory queues from which tasks can be retrieved.
 *
 * @param <E> the base exception class for all exceptions that can be thrown from this API
 * @param <C> the enumeration for all error codes specified by this API
 * @param <I> the type of information associated with a task
 * @param <P> the type of pojo used to persist tasks from this queue
 * @param <T> the type of task that can be retrieved from this queue
 */
public interface InMemoryQueue<
        E extends Exception,
        C extends ErrorCode,
        I extends TaskInfo,
        P extends AbstractTaskPojo<P>,
        T extends InMemoryTask<E, C, I, P>>
    extends Queue<E, C, T> {
  /**
   * Gets the broker where this queue is currently defined.
   *
   * @param <B> the type of broker this queue is currently defined in
   * @param <O> the base type of all offering queues managed by the broker
   * @param <Q> the base type for all queues managed by the broker
   * @param <S> the base type for all composite queues managed by the broker
   * @return the broker where this queue is currently defined
   */
  public <
          B extends InMemoryQueueBroker<E, C, I, P, O, Q, S, T>,
          O extends InMemoryOfferingQueue<E, C, I, P, T>,
          Q extends InMemoryQueue<E, C, I, P, T>,
          S extends InMemoryCompositeQueue<E, C, I, P, O, T>>
      B getBroker();

  /**
   * Gets the number of tasks in this queue.
   *
   * @return the number of tasks in this queue
   */
  public int size();

  /**
   * Gets the number of tasks in this queue in the specified transitional state (see {@link
   * State#isTransitional()}).
   *
   * @param state the transitional state for which to get the number of tasks currently in that
   *     state
   * @return the number of tasks in this queue in the specified state
   * @throws IllegalArgumentException if called with a final state
   */
  public int size(State state);

  /**
   * Gets the number of additional tasks that this queue can ideally (in the absence of memory or
   * resource constraints) accept without blocking. This is always equal to the initial capacity of
   * this queue minus the current {@link #size()} of this queue.
   *
   * <p><i>Note:</i> You <em>cannot</em> always tell if an attempt to insert an element will succeed
   * by inspecting {@link #remainingCapacity} because it may be the case that another thread is
   * about to insert or remove an element.
   *
   * @return the current remaining capacity
   */
  public int remainingCapacity();

  /** Clear the queue by removing all tasks currently defined in them. */
  public void clear();
}

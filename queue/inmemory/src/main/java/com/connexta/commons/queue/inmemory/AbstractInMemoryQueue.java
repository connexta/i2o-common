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
import com.connexta.commons.queue.TaskInfo;
import com.connexta.commons.queue.impl.persistence.AbstractTaskPojo;
import io.micrometer.core.instrument.Tag;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base class for all type of queues from which tasks can be retrieved.
 *
 * @param <E> the base exception class for all exceptions that can be thrown from this API
 * @param <C> the enumeration for all error codes specified by this API
 * @param <I> the type of information associated with a task
 * @param <P> the type of pojo used to persist tasks from this queue
 * @param <T> the type of task that can be retrieved from this queue
 */
public abstract class AbstractInMemoryQueue<
        E extends Exception,
        C extends ErrorCode,
        I extends TaskInfo,
        P extends AbstractTaskPojo<P>,
        T extends InMemoryTask<E, C, I, P>>
    implements Queue<E, C, T>, InMemoryQueue<E, C, I, P, T> {
  protected final InMemoryQueueBroker<
          E,
          C,
          I,
          P,
          ? extends InMemoryOfferingQueue<E, C, I, P, T>,
          ? extends InMemoryQueue<E, C, I, P, T>,
          ? extends InMemoryCompositeQueue<E, C, I, P, ?, T>,
          T>
      broker;
  protected final Set<Tag> tags;

  /**
   * Instantiates a new queue from the specified broker.
   *
   * @param <B> the type of broker this queue is currently defined in
   * @param <O> the base type of all offering queues managed by the broker
   * @param <Q> the base type for all queues managed by the broker
   * @param <S> the base type for all composite queues managed by the broker
   * @param broker the broker for which we are instantiating this queue
   */
  public <
          B extends InMemoryQueueBroker<E, C, I, P, O, Q, S, T>,
          O extends InMemoryOfferingQueue<E, C, I, P, T>,
          Q extends InMemoryQueue<E, C, I, P, T>,
          S extends InMemoryCompositeQueue<E, C, I, P, O, T>>
      AbstractInMemoryQueue(B broker) {
    this.broker = broker;
    this.tags = broker.tags;
  }

  /**
   * Instantiates a new queue from the specified broker.
   *
   * @param <B> the type of broker this queue is currently defined in
   * @param <O> the base type of all offering queues managed by the broker
   * @param <Q> the base type for all queues managed by the broker
   * @param <S> the base type for all composite queues managed by the broker
   * @param broker the broker for which we are instantiating this queue
   * @param tags set of additional tags to use with all metrics generated by this queue
   */
  public <
          B extends InMemoryQueueBroker<E, C, I, P, O, Q, S, T>,
          O extends InMemoryOfferingQueue<E, C, I, P, T>,
          Q extends InMemoryQueue<E, C, I, P, T>,
          S extends InMemoryCompositeQueue<E, C, I, P, O, T>>
      AbstractInMemoryQueue(B broker, Stream<Tag> tags) {
    this.broker = broker;
    this.tags = Stream.concat(broker.tags.stream(), tags).collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public <
          B extends InMemoryQueueBroker<E, C, I, P, O, Q, S, T>,
          O extends InMemoryOfferingQueue<E, C, I, P, T>,
          Q extends InMemoryQueue<E, C, I, P, T>,
          S extends InMemoryCompositeQueue<E, C, I, P, O, T>>
      B getBroker() {
    return (B) broker;
  }
}
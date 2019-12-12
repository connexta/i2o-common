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

import com.connexta.commons.queue.CompositeQueue;
import com.connexta.commons.queue.ErrorCode;
import com.connexta.commons.queue.Task.State;
import com.connexta.commons.queue.TaskInfo;
import com.connexta.commons.queue.impl.persistence.AbstractTaskPojo;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * A composite queue is an artifact created around multiple site queues allowing a worker to
 * retrieve tasks from any of the queues based on task priorities.
 *
 * @param <E> the base exception class for all exceptions that can be thrown from this API
 * @param <C> the enumeration for all error codes specified by this API
 * @param <I> the type of information associated with a task
 * @param <P> the type of pojo used to persist tasks managed by this queue
 * @param <O> the type of queues managed by this composite queue
 * @param <T> the type of task that can be retrieved from this queue
 */
public class InMemoryCompositeQueue<
        E extends Exception,
        C extends ErrorCode,
        I extends TaskInfo,
        P extends AbstractTaskPojo<P>,
        O extends InMemoryOfferingQueue<E, C, I, P, T>,
        T extends InMemoryTask<E, C, I, P>>
    extends AbstractInMemoryQueue<E, C, I, P, T> implements CompositeQueue<E, C, I, O, T> {
  private final Set<Id> ids;

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
      InMemoryCompositeQueue(B broker, Stream<Id> ids) {
    super(broker);
    this.ids = ids.collect(Collectors.toSet());
  }

  @Override
  public Stream<O> queues() {
    return ids.stream().map(broker::getQueueIfDefined).flatMap(Optional::stream).map(q -> (O) q);
  }

  @Override
  public Stream<Id> ids() {
    return ids.stream();
  }

  @Override
  public Optional<O> getQueue(Id id) {
    return ids.contains(id) ? (Optional<O>) broker.getQueueIfDefined(id) : Optional.empty();
  }

  @Override
  public int size() {
    return queues().mapToInt(AbstractInMemoryQueue::size).sum();
  }

  @Override
  public int size(State state) {
    return queues().mapToInt(q -> q.size(state)).sum();
  }

  @Override
  public int remainingCapacity() {
    return queues().mapToInt(AbstractInMemoryQueue::remainingCapacity).sum();
  }

  @Override
  public T take() throws InterruptedException, E {
    throw new UnsupportedOperationException("not yet supported");
  }

  @Nullable
  @Override
  public T poll(long timeout, TimeUnit unit) throws InterruptedException, E {
    throw new UnsupportedOperationException("not yet supported");
  }

  @Override
  public void clear() {
    queues().forEach(AbstractInMemoryQueue::clear);
  }
}

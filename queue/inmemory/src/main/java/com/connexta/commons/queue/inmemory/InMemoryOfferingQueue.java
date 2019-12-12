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
import com.connexta.commons.queue.OfferingQueue;
import com.connexta.commons.queue.Task;
import com.connexta.commons.queue.Task.State;
import com.connexta.commons.queue.TaskInfo;
import com.connexta.commons.queue.impl.persistence.AbstractTaskPojo;
import com.google.common.annotations.VisibleForTesting;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * Provides an in-memory implementation for a specific queue.
 *
 * @param <E> the base exception class for all exceptions that can be thrown from this API
 * @param <C> the enumeration for all error codes specified by this API
 * @param <I> the type of information associated with a task
 * @param <P> the type of pojo used to persist tasks managed by this queue
 * @param <T> the type of task that can be retrieved from this queue
 */
// locking behavior is similar to LinkedBlockingQueue such that we can control capacity
// and size across all internal queues and also to properly handle polling from multiple
// different queues
public abstract class InMemoryOfferingQueue<
        E extends Exception,
        C extends ErrorCode,
        I extends TaskInfo,
        P extends AbstractTaskPojo<P>,
        T extends InMemoryTask<E, C, I, P>>
    extends AbstractInMemoryQueue<E, C, I, P, T> implements OfferingQueue<E, C, I, T> {
  private final Id id;

  /** List of all internal queues indexed by their corresponding priorities. */
  private final List<Deque<InMemoryTask<E, C, I, P>>> pendings = new ArrayList<>();

  /** Ordered set of processing tasks that are still locked and being worked on. */
  private final Set<InMemoryTask<E, C, I, P>> processing = new LinkedHashSet<>();

  /** The capacity bound, or Integer.MAX_VALUE if none. */
  private final int capacity;

  /** Current number of tasks. */
  private final AtomicInteger count;

  /** Current number of pending tasks. */
  private final AtomicInteger pendingCount;

  /** Current number of processing tasks. */
  private final AtomicInteger processingCount;

  /** Lock held by take, poll, etc. */
  private final ReentrantLock takeLock = new ReentrantLock();

  /** Wait queue for waiting takes. */
  private final Condition notEmpty = takeLock.newCondition();

  /** Lock held by put, offer, etc. */
  private final ReentrantLock putLock = new ReentrantLock();

  /** Wait queue for waiting puts. */
  private final Condition notFull = putLock.newCondition();

  /**
   * Flag used while testing to let us know that we should signal all waiters on the <code>notEmpty
   * </code> condition whenever the <code>count</code> or <code>pendingCount</code> is affected and
   * not just when it is no longer empty. This is primarily used when testing with this queue
   * implementation.
   */
  private volatile boolean waitingForCountToChange = false;

  /**
   * Optional lists of suspended signals. This is initialized during testing when a test calls
   * {@link #suspendSignals} such that any conditions that would normally signal threads that are
   * currently waiting on them be recorded in the referenced list to be played when {@link
   * #resumeSignals} is finally called.
   */
  private java.util.Queue<Runnable> suspendedSignals = null; // not suspended to start with

  private final Counter queuedCounter;
  private final Counter requeuedCounter;
  private final Counter successCounter;
  private final Counter failCounter;
  private final Timer timer;
  private final List<Timer> pendingTimers = new ArrayList<>(Task.Constants.MAX_PRIORITY + 1);
  private final Timer processingTimer;

  /**
   * Instantiates a default in-memory queue.
   *
   * @param <B> the type of broker this queue is currently defined in
   * @param <O> the base type of all offering queues managed by the broker
   * @param <Q> the base type for all queues managed by the broker
   * @param <S> the base type for all composite queues managed by the broker
   * @param broker the broker for which we are instantiating this queue
   * @param id the corresponding queue id
   */
  public <
          B extends InMemoryQueueBroker<E, C, I, P, O, Q, S, T>,
          O extends InMemoryOfferingQueue<E, C, I, P, T>,
          Q extends InMemoryQueue<E, C, I, P, T>,
          S extends InMemoryCompositeQueue<E, C, I, P, O, T>>
      InMemoryOfferingQueue(B broker, Id id) {
    super(broker, Stream.concat(broker.metricTags(), Stream.of(Tag.of("id", id.getId()))));
    this.id = id;
    this.capacity = broker.getQueueCapacity();
    this.count =
        broker.meterRegistry.gauge(broker.prefix + ".queue.tasks", super.tags, new AtomicInteger());
    // no need to gauge this one since the priority gauges below will report that automatically
    this.pendingCount = new AtomicInteger();
    this.processingCount =
        broker.meterRegistry.gauge(
            broker.prefix + ".queue.processing.tasks", super.tags, new AtomicInteger());
    for (int i = Task.Constants.MIN_PRIORITY; i <= Task.Constants.MAX_PRIORITY; i++) {
      final Iterable<Tag> ptags =
          Stream.concat(super.tags.stream(), Stream.of(Tag.of("priority", Integer.toString(i))))
              .collect(Collectors.toSet());

      pendings.add(
          broker.meterRegistry.gaugeCollectionSize(
              broker.prefix + ".queue.pending.tasks", ptags, new LinkedList<>()));
      this.pendingTimers.add(
          broker.meterRegistry.timer(broker.prefix + ".queue.pending.timer", ptags));
    }
    this.queuedCounter =
        broker.meterRegistry.counter(broker.prefix + ".queue.queued.tasks", super.tags);
    this.requeuedCounter =
        broker.meterRegistry.counter(broker.prefix + ".queue.requeued.tasks", super.tags);
    this.successCounter =
        broker.meterRegistry.counter(broker.prefix + ".queue.success.tasks", super.tags);
    this.failCounter =
        broker.meterRegistry.counter(broker.prefix + ".queue.fail.tasks", super.tags);
    this.timer = broker.meterRegistry.timer(broker.prefix + ".queue.overall.timer", super.tags);
    this.processingTimer =
        broker.meterRegistry.timer(broker.prefix + ".queue.processing.timer", super.tags);
  }

  @Override
  public Id getId() {
    return id;
  }

  @Override
  public int size() {
    return count.get();
  }

  @Override
  public int size(State state) {
    switch (state) {
      case PENDING:
        return pendingCount.get();
      case PROCESSING:
        return processingCount.get();
      default:
        throw new IllegalArgumentException("not a transitional state: " + state);
    }
  }

  @Override
  public int remainingCapacity() {
    return capacity - size();
  }

  @Override
  public void put(I info) throws InterruptedException, E {
    putAndPeek(info);
  }

  @Override
  public boolean offer(I info) throws E {
    if (pendingCount.get() == capacity) {
      return false;
    }
    final T task = createTask(info);
    final Deque<InMemoryTask<E, C, I, P>> queue = pendings.get(task.getPriority());
    final int c;

    putLock.lock();
    try {
      if (pendingCount.get() >= capacity) {
        return false;
      }
      queue.addLast(task);
      if (count.incrementAndGet() < capacity) {
        signalNotFullWhenAlreadyLocked();
      }
      c = pendingCount.getAndIncrement();
    } finally {
      putLock.unlock();
    }
    signalNotEmpty(c == 0);
    queuedCounter.increment();
    return true;
  }

  @Override
  public boolean offer(I info, long timeout, TimeUnit unit) throws InterruptedException, E {
    final InMemoryTask<E, C, I, P> task = createTask(info);
    final Deque<InMemoryTask<E, C, I, P>> queue = pendings.get(task.getPriority());
    long duration = unit.toNanos(timeout);
    final int c;

    putLock.lockInterruptibly();
    try {
      while (remainingCapacity() <= 0) {
        if (duration <= 0L) {
          return false;
        }
        duration = notFull.awaitNanos(duration);
      }
      queue.addLast(task);
      if (count.incrementAndGet() < capacity) {
        signalNotFullWhenAlreadyLocked();
      }
      c = pendingCount.getAndIncrement();
    } finally {
      putLock.unlock();
    }
    signalNotEmpty(c == 0);
    queuedCounter.increment();
    return true;
  }

  @Override
  public T take() throws InterruptedException {
    final T task;

    takeLock.lockInterruptibly();
    try {
      while (pendingCount.get() == 0) {
        notEmpty.await();
      }
      // cannot return null since pendingCount is not 0, we are looking at all priority queues,
      // and we are locked
      task = pollFirst(Task.Constants.MIN_PRIORITY);
      final int c = pendingCount.getAndDecrement();

      signalNotEmptyWhenAlreadyLocked(c > 1); // still some pending ones so let others know
    } finally {
      takeLock.unlock();
    }
    return task;
  }

  @Nullable
  @Override
  public T poll(long timeout, TimeUnit unit) throws InterruptedException {
    return poll(Task.Constants.MIN_PRIORITY, timeout, unit);
  }

  /**
   * Inserts the specified task with its given priority into this queue, waiting if necessary for
   * space to become available.
   *
   * @param info the info for the task to be added to the queue
   * @return the corresponding task that was added to the queue
   * @throws InterruptedException if the current thread was interrupted while waiting to insert the
   *     task
   * @throws E if an error occurred while performing the operation
   */
  public T putAndPeek(I info) throws InterruptedException, E {
    final T task = createTask(info);
    final Deque<InMemoryTask<E, C, I, P>> queue = pendings.get(task.getPriority());
    final int c;

    putLock.lockInterruptibly();
    try {
      // Note that count is used in wait guard even though it is not protected by lock. This works
      // because count can only decrease at this point (all other puts are shut out by lock), and we
      // (or some other waiting put) are signalled if it ever changes from capacity. Similarly for
      // all other uses of count in other wait guards.
      while (remainingCapacity() <= 0) {
        notFull.await();
      }
      queue.addLast(task);
      if (count.incrementAndGet() < capacity) {
        signalNotFullWhenAlreadyLocked();
      }
      c = pendingCount.getAndIncrement();
    } finally {
      putLock.unlock();
    }
    signalNotEmpty(c == 0);
    queuedCounter.increment();
    return task;
  }

  @Override
  public void clear() {
    // suspendSignals(), resumeSignals(), and clear() are the only place we lock on both so make
    // sure to do it in the same order
    takeLock.lock();
    putLock.lock();
    try {
      pendings.forEach(Deque::clear);
      pendingCount.set(0);
      processing.clear();
      processingCount.set(0);
      count.set(0);
      signalNotFullWhenAlreadyLocked();
    } finally {
      putLock.unlock();
      takeLock.unlock();
    }
  }

  /**
   * Peeks and returns the first pending task in this queue.
   *
   * <p><i>Note:</i> This would be the task with the highest priority that was added first among all
   * the pending tasks. The returned task will not be locked and will report a {@link State#PENDING}
   * state.
   *
   * @return the first pending task in this queue or <code>null</code> if none available
   */
  @Nullable
  public T peekFirst() {
    if (count.get() == 0) {
      return null;
    }
    takeLock.lock();
    try {
      if (count.get() > 0) {
        for (int i = Task.Constants.MAX_PRIORITY; i >= Task.Constants.MIN_PRIORITY; i--) {
          final InMemoryTask<E, C, I, P> task = pendings.get(i).peekFirst();

          if (task != null) {
            return (T) task;
          }
        }
      }
      return null;
    } finally {
      takeLock.unlock();
    }
  }

  /**
   * Peeks and returns the last pending task in this queue.
   *
   * <p><i>Note:</i> This would be the task with the lowest priority that was added first among all
   * the pending tasks. The returned task will not be locked and will report a {@link State#PENDING}
   * state.
   *
   * @return the last pending task in this queue or <code>null</code> if none available
   */
  @Nullable
  public T peekLast() {
    if (count.get() == 0) {
      return null;
    }
    takeLock.lock();
    try {
      if (count.get() > 0) {
        for (int i = Task.Constants.MIN_PRIORITY; i <= Task.Constants.MAX_PRIORITY; i++) {
          final InMemoryTask<E, C, I, P> task = pendings.get(i).peekLast();

          if (task != null) {
            return (T) task;
          }
        }
      }
      return null;
    } finally {
      takeLock.unlock();
    }
  }

  /**
   * Waits for a number of pending tasks in the queue to reach a given threshold before returning.
   *
   * @param size the number of pending tasks that should be in this queue before returning
   * @param timeout how long to wait before giving up, in units of <code>unit</code>
   * @param unit a {@code TimeUnit} determining how to interpret the <code>timeout</code> parameter
   * @return <code>true</code> if the specified size was reached in time; <code>false</code> if we
   *     timed out waiting
   * @throws InterruptedException if the current thread was interrupted while waiting
   */
  public boolean waitForPendingSizeToReach(int size, long timeout, TimeUnit unit)
      throws InterruptedException {
    return waitToReach(pendingCount, size, timeout, unit);
  }

  /**
   * Waits for a number of tasks in the queue to reach a given threshold before returning.
   *
   * @param size the number of tasks that should be in this queue before returning
   * @param timeout how long to wait before giving up, in units of <code>unit</code>
   * @param unit a {@code TimeUnit} determining how to interpret the <code>timeout</code> parameter
   * @return <code>true</code> if the specified size was reached in time; <code>false</code> if we
   *     timed out waiting
   * @throws InterruptedException if the current thread was interrupted while waiting
   */
  public boolean waitForSizeToReach(int size, long timeout, TimeUnit unit)
      throws InterruptedException {
    return waitToReach(count, size, timeout, unit);
  }

  /**
   * Suspends all internal signals which might otherwise wakeup threads waiting on specific
   * conditions until such time when {@link #resumeSignals} is called.
   *
   * <p><i>Note:</i> This method can be useful during testing when the side effect of completing
   * tasks could allow parallel threads to start offering new ones. By suspending signals, these
   * threads will remained blocked until such time when the test is ready to have them resume
   * normally.
   */
  public void suspendSignals() {
    // suspendSignals(), resumeSignals(), and clear() are the only place we lock on both so make
    // sure to do it in the same order
    takeLock.lock();
    putLock.lock();
    try {
      if (suspendedSignals == null) {
        this.suspendedSignals = new ConcurrentLinkedQueue<>();
      } // else - no-op, continue with the list we had
    } finally {
      putLock.unlock();
      takeLock.unlock();
    }
  }

  /**
   * Resumes all suspended signals while first processing those that have been accumulated.
   *
   * <p><i>Note:</i> This method can be useful during testing when the side effect of completing
   * tasks could allow parallel threads to start offering new ones. By resuming previously suspended
   * signals, these threads will finally continue processing as they would have in the first place
   * when the condition actually happened.
   */
  public void resumeSignals() {
    // suspendSignals(), resumeSignals(), and clear() are the only place we lock on both so make
    // sure to do it in the same order
    takeLock.lock();
    putLock.lock();
    try {
      if (suspendedSignals != null) { // play all accumulated signals
        suspendedSignals.stream().forEach(Runnable::run);
        this.suspendedSignals = null;
      }
    } finally {
      putLock.unlock();
      takeLock.unlock();
    }
  }

  /**
   * Called to create a new task given the specified information.
   *
   * @param info the information for the task to be created
   * @return a corresponding newly created task
   * @throws E if an error occurred while performing the operation
   */
  protected abstract T createTask(I info) throws E;

  /**
   * Retrieves and removes the next available highest priority task with a priority equal or greater
   * than the specified one, waiting up to the specified wait time if necessary for one to become
   * available. The returned task is considered locked by the current thread/worker.
   *
   * @param minPriority the minimum priority for which to retrieve a task (0 to 9)
   * @param timeout how long to wait before giving up, in units of <code>unit</code>
   * @param unit a {@code TimeUnit} determining how to interpret the <code>timeout</code> parameter
   * @return the next available task with the highest priority from this queue or <code>null</code>
   *     if the specified amount time elapsed before a task is available
   * @throws InterruptedException if the current thread was interrupted while waiting for a task to
   *     be returned
   */
  @Nullable
  T poll(int minPriority, long timeout, TimeUnit unit) throws InterruptedException {
    final T task;
    long duration = unit.toNanos(timeout);

    takeLock.lockInterruptibly();
    try {
      while (pendingCount.get() == 0) {
        if (duration <= 0L) {
          return null;
        }
        duration = notEmpty.awaitNanos(duration);
      }
      // cannot return null since pendingCount is not 0, we are looking at all priority queues,
      // and we are locked
      task = pollFirst(minPriority);
      final int c = pendingCount.getAndDecrement();

      signalNotEmptyWhenAlreadyLocked(c > 1); // still some pending ones so let others know
    } finally {
      takeLock.unlock();
    }
    return task;
  }

  /**
   * Called by {@link InMemoryTask} when the task's owner indicates the task should be removed from
   * the queue.
   *
   * @param task the task that should be removed
   * @param whenRemoved a callback that will be called right after having removed the task from the
   *     queue (it is expected that the task durations will be calculated at that point)
   * @throws InterruptedException if the current thread is interrupted while attempting to remove
   *     the processing task
   */
  void remove(InMemoryTask<E, C, I, P> task, Runnable whenRemoved) throws InterruptedException {
    int c;

    takeLock.lockInterruptibly();
    try {
      processing.remove(task);
      processingCount.decrementAndGet();
      c = count.getAndDecrement();
      whenRemoved.run();
      timer.record(task.getDuration().toMillis(), TimeUnit.MILLISECONDS);
      pendingTimers
          .get(task.getPriority())
          .record(task.getDuration(State.PENDING).toMillis(), TimeUnit.MILLISECONDS);
      processingTimer.record(task.getDuration(State.PROCESSING).toMillis(), TimeUnit.MILLISECONDS);
      signalNotEmptyWhenAlreadyLocked(false); // change to count so signal all waiters
    } finally {
      takeLock.unlock();
    }
    if (c == capacity) {
      signalNotFull();
    }
    (task.isFailed() ? failCounter : successCounter).increment();
  }

  /**
   * Called by {@link InMemoryTask} when the task's owner indicates the task should be retried
   * later.
   *
   * @param task the task that needs to be re-queued
   * @param atFront <code>true</code> to re-queue the task at the front of the queue; <code>false
   *     </code> to re-queue at the end
   * @param whenQueued a callback that will be called just before re-inserting the task in the queue
   * @throws InterruptedException if the current thread is interrupted while attempting to re-queue
   *     the processing task
   */
  void requeue(InMemoryTask<E, C, I, P> task, boolean atFront, Runnable whenQueued)
      throws InterruptedException {
    takeLock.lockInterruptibly();
    // start by removing it from the processing list
    try {
      processing.remove(task);
      processingCount.decrementAndGet();
    } finally {
      takeLock.unlock();
    }
    // now re-insert it at the end or front of the queue
    final Deque<InMemoryTask<E, C, I, P>> queue = pendings.get(task.getPriority());
    final int c;

    putLock.lock(); // don't allow interruption here as we want to complete the operation
    try {
      whenQueued.run();
      if (atFront) {
        queue.addFirst(task);
      } else {
        queue.addLast(task);
      }
      c = pendingCount.getAndIncrement();
      if (count.get() < capacity) {
        signalNotFullWhenAlreadyLocked();
      }
    } finally {
      putLock.unlock();
    }
    signalNotEmpty(c == 0);
    requeuedCounter.increment();
  }

  @VisibleForTesting
  IntStream pendingSizes() {
    return pendings.stream().mapToInt(Deque::size);
  }

  @VisibleForTesting
  int pendingSize(int priority) {
    return pendings.get(priority).size();
  }

  @VisibleForTesting
  boolean waitForOfferorsToBeBlocked(long timeout) throws InterruptedException {
    final long start = System.nanoTime();
    final long nanos = TimeUnit.MILLISECONDS.toNanos(timeout);

    while (true) {
      putLock.lockInterruptibly();
      try {
        if (putLock.hasWaiters(notFull)) {
          return true;
        } else if ((System.nanoTime() - start) >= nanos) {
          return false;
        }
        Thread.onSpinWait();
      } finally {
        putLock.unlock();
      }
    }
  }

  @VisibleForTesting
  boolean waitForPullersToBeBlocked(long timeout) throws InterruptedException {
    final long start = System.nanoTime();
    final long nanos = TimeUnit.MILLISECONDS.toNanos(timeout);

    while (true) {
      takeLock.lockInterruptibly();
      try {
        if (takeLock.hasWaiters(notEmpty)) {
          return true;
        } else if ((System.nanoTime() - start) >= nanos) {
          return false;
        }
        Thread.onSpinWait();
      } finally {
        takeLock.unlock();
      }
    }
  }

  /**
   * Waits for a given count of tasks in the queue to reach a given threshold before returning.
   *
   * @param count the count to monitor
   * @param size the number of tasks reported by the count that should be in this queue before
   *     returning
   * @param timeout how long to wait before giving up, in units of <code>unit</code>
   * @param unit a {@code TimeUnit} determining how to interpret the <code>timeout</code> parameter
   * @return <code>true</code> if the specified size was reached in time; <code>false</code> if we
   *     timed out waiting
   * @throws InterruptedException if the current thread was interrupted while waiting
   */
  private boolean waitToReach(AtomicInteger count, int size, long timeout, TimeUnit unit)
      throws InterruptedException {
    long duration = unit.toNanos(timeout);

    takeLock.lockInterruptibly();
    try {
      this.waitingForCountToChange = true;
      while (count.get() != size) {
        if (duration <= 0L) {
          return false;
        }
        duration = notEmpty.awaitNanos(duration);
      }
      return true;
    } finally {
      this.waitingForCountToChange = false;
      takeLock.unlock();
    }
  }

  /**
   * Retrieves and removes the first task available from the highest priority queue which is greater
   * or equal to the specified priority.
   *
   * <p><i>Note:</i> It is assumed that the caller of this method has acquired the <code>taskLock
   * </code> prior to calling this method and that at least one task is available in the queue (
   * <code>count > 0</code>).
   *
   * @param minPriority the minimum priority for which to remove a task (0 to 9)
   * @return the first available highest priority task with a priority no less than the specified
   *     priority (task will be locked to the current thread) or <code>null</code> if no tasks are
   *     available at or above the specified priority
   */
  private T pollFirst(int minPriority) {
    for (int i = Task.Constants.MAX_PRIORITY; i >= minPriority; i--) {
      final InMemoryTask<E, C, I, P> task = pendings.get(i).pollFirst();

      if (task != null) {
        processing.add(task);
        processingCount.getAndIncrement();
        task.lock();
        return (T) task;
      }
    }
    return null;
  }

  /**
   * Signals a waiting take. Called only from put/offer (which do not otherwise ordinarily lock
   * takeLock).
   *
   * @param signal <code>true</code> to signal waiting takes; <code>false</code> not to (unless we
   *     are testing and some threads are waiting on count changes)
   */
  private void signalNotEmpty(boolean signal) {
    if (waitingForCountToChange || signal) {
      takeLock.lock();
      try {
        if (suspendedSignals != null) {
          // assume we will be locked when signal processing is resumed
          suspendedSignals.add(this::signalNotEmptyDirectly);
        } else {
          signalNotEmptyDirectly();
        }
      } finally {
        takeLock.unlock();
      }
    }
  }

  private void signalNotEmptyWhenAlreadyLocked(boolean signal) {
    if (waitingForCountToChange || signal) {
      if (suspendedSignals != null) {
        suspendedSignals.add(this::signalNotEmptyDirectly);
      } else {
        signalNotEmptyDirectly();
      }
    }
  }

  private void signalNotEmptyDirectly() {
    if (waitingForCountToChange) { // signal all waiters of the change
      notEmpty.signalAll();
    } else { // signal only one waiter
      notEmpty.signal();
    }
  }

  /** Signals a waiting put. Called only from take/poll. */
  private void signalNotFull() {
    putLock.lock();
    try {
      if (suspendedSignals != null) {
        // assume we will be locked when signal processing is resumed
        suspendedSignals.add(notFull::signal);
      } else {
        notFull.signal();
      }
    } finally {
      putLock.unlock();
    }
  }

  private void signalNotFullWhenAlreadyLocked() {
    if (suspendedSignals != null) {
      suspendedSignals.add(notFull::signal);
    } else {
      notFull.signal();
    }
  }
}

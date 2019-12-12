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
import com.connexta.commons.queue.TaskInfo;
import com.connexta.commons.queue.impl.AbstractTask;
import com.connexta.commons.queue.impl.persistence.AbstractTaskPojo;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

/**
 * Provides a base in-memory implementation for the {@link com.connexta.commons.queue.Task}
 * interface.
 *
 * @param <E> the base exception class for all exceptions that can be thrown from this API
 * @param <C> the enumeration for all error codes specified by this API
 * @param <I> the type of information associated with this task
 * @param <P> the type of pojo used to persist this task
 */
public abstract class InMemoryTask<
        E extends Exception, C extends ErrorCode, I extends TaskInfo, P extends AbstractTaskPojo<P>>
    extends AbstractTask<E, C, I, P> {
  private final InMemoryOfferingQueue<E, C, I, P, ? extends InMemoryTask<E, C, I, P>> queue;
  private final I info;

  private final ReentrantLock lock = new ReentrantLock();

  @SuppressWarnings("squid:S3077" /* variable only used for reference checking */)
  @Nullable
  private volatile Thread owner = null;

  /**
   * Creates a new task for the corresponding object as it is being queued to a given queue.
   *
   * @param persistableType a string representing the type of this object (used when generating
   *     exception or logs)
   * @param queue the queue where the task is being queued
   * @param info the info for which to create a task
   */
  public InMemoryTask(
      String persistableType,
      InMemoryOfferingQueue<E, C, I, P, ? extends InMemoryTask<E, C, I, P>> queue,
      I info) {
    super(
        persistableType,
        queue.broker.eclazz,
        queue.broker.iecreator,
        queue.broker.uvecreator,
        info.getPriority(),
        queue.broker.meterRegistry.config().clock());
    this.queue = queue;
    this.info = info;
  }

  @Override
  public I getInfo() {
    return info;
  }

  @Override
  public boolean isLocked() {
    return lock.isHeldByCurrentThread();
  }

  /**
   * Gets the thread that is currently processing this task.
   *
   * <p><i>Note:</i> This is a best effort since ownership can change at any point in time.
   *
   * @return the thread currently processing the task or empty if the task is not currently being
   *     processed
   */
  public Optional<Thread> getOwner() {
    return Optional.ofNullable(owner);
  }

  @Override
  public void unlock() throws InterruptedException {
    verifyCompletionAndOwnership();
    // requeue in front of the queue and leave queue time as is since the owner didn't work on it
    // and is just unlocking it for another worker to pick it up
    queue.requeue(this, true, () -> requeued(null, null));
  }

  @Override
  public void complete() throws InterruptedException {
    verifyCompletionAndOwnership();
    queue.remove(this, () -> removed(State.SUCCESSFUL, null, null));
  }

  @Override
  public void fail(C reason) throws InterruptedException {
    fail(reason, null);
  }

  @Override
  public void fail(C reason, @Nullable String message) throws InterruptedException {
    verifyCompletionAndOwnership();
    if (reason.isRetryable()) {
      // re-queue at end of the queue and update it once it is queued
      queue.requeue(this, false, () -> requeued(reason, message));
    } else { // done so just remove the task and mark it complete
      queue.remove(this, () -> removed(State.FAILED, reason, message));
    }
  }

  @Override
  public int hashCode() {
    return hashCode00();
  }

  @Override
  public boolean equals(Object obj) {
    return equals00(obj);
  }

  @Override
  public String toString() {
    return String.format(
        "InMemoryTask[id=%s, priority=%d, info=%s, originalQueuedTime=%s, attempts=%d, queuedTime=%s, state=%s, failureReason=%s, failureMessage=%s, duration=%s, durations=%s, thawedTime=%s]",
        getId(),
        getPriority(),
        getInfo(),
        getOwner(),
        getAttempts(),
        getQueuedTime(),
        getState(),
        getFailureReason(),
        getFailureMessage(),
        getDuration(),
        getDurations(),
        getThawedTime());
  }

  /**
   * Verifies if the task has not been completed or if it is currently lock by the current thread.
   *
   * @throws IllegalStateException if the task was already marked completed via one of {@link
   *     #complete()}, {@link #fail(C)}, or {@link #fail(C, String)}
   * @throws IllegalMonitorStateException if the current thread/worker doesn't own the lock to the
   *     task or if the task is not currently locked
   */
  protected void verifyCompletionAndOwnership() {
    if (isSuccessful()) {
      throw new IllegalStateException("task already completed successfully");
    } else if (isFailed()) {
      throw new IllegalStateException("task already failed");
    } else if (Thread.currentThread() != owner) {
      throw new IllegalMonitorStateException();
    }
  }

  /**
   * Callback passed to the {@link InMemoryOfferingQueue} when removing a task to update the
   * internal state of the task after the task was actually removed from the queue.
   *
   * @param state the new state to record
   * @param reason the reason for the failure or <code>null</code> if the new state doesn't
   *     represent a failure
   * @param message the optional message associated with the failure or <code>null</code> if none
   *     provided
   */
  protected void removed(State state, @Nullable C reason, @Nullable String message) {
    setState(state, reason, message);
    lock.unlock(); // should not fail since we already checked in the complete() and fail() methods
    this.owner = null;
  }

  /**
   * Callback passed to the {@link InMemoryOfferingQueue} when re-queuing a task to update the
   * internal state of the task after the task is about to be re-added to the queue.
   *
   * @param reason the reason for the failure or <code>null</code> if the new state doesn't
   *     represent a failure
   * @param message the optional message associated with the failure or <code>null</code> if none
   *     provided
   */
  protected void requeued(@Nullable C reason, @Nullable String message) {
    setState(State.PENDING, reason, message);
    lock.unlock(); // should not fail since we already checked in the unlock() and fail() methods
    this.owner = null;
  }

  /** Called by the associated {@link InMemoryOfferingQueue} to lock this task. */
  void lock() {
    setState(State.PROCESSING, null, null);
    lock.lock();
    this.owner = Thread.currentThread();
  }

  @VisibleForTesting
  int hashCode00() {
    return Objects.hash(super.hashCode(), info);
  }

  @VisibleForTesting
  boolean equals00(Object obj) {
    if (super.equals(obj) && (obj instanceof InMemoryTask)) {
      final InMemoryTask task = (InMemoryTask) obj;

      return Objects.equals(info, task.info);
    }
    return false;
  }
}

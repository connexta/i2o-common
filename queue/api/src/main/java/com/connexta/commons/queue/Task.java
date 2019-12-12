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

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

/**
 * Represents a task that can be queued for later processing.
 *
 * @param <E> the base exception class for all exceptions that can be thrown from this API
 * @param <C> the enumeration for all error codes specified by this API
 */
public interface Task<E extends Exception, C extends ErrorCode> extends TaskInfo {
  /**
   * Gets the number of times this particular task has been attempted. When a task fails for a
   * reason that doesn't preclude it from being re-tried and it gets re-queued by the worker, its
   * total attempts counter will automatically be incremented as it is re-added to the associated
   * queue for later reprocessing.
   *
   * <p><i>Note:</i> This method will return <code>1</code> when first queued to indicate it is in
   * its first attempt.
   *
   * @return the total number of times this task has been attempted so far
   */
  public int getAttempts();

  /**
   * Gets the current state of the task.
   *
   * @return the current task state
   */
  public State getState();

  /**
   * Checks if this task has been marked completed.
   *
   * <p><i>Note:</i> A task that fails for reasons that allows for it to be retried later will not
   * be reported as completed. It will be reported as completed for any other failure reasons.
   *
   * @return <code>true</code> if this task has been marked completed; <code>false</code> otherwise
   */
  public default boolean isCompleted() {
    return getState().isFinal();
  }

  /**
   * Checks if this task has successfully completed.
   *
   * @return <code>true</code> if this task has successfully completed; <code>false</code> otherwise
   */
  public default boolean isSuccessful() {
    return (getState() == State.SUCCESSFUL);
  }

  /**
   * Checks if this task has completed with a failure that doesn't allow for it to be retried later.
   *
   * <p><i>Note:</i> A task that fails for reasons that allows for it to be retried later will not
   * be reported as failed.
   *
   * @return <code>true</code> if this task has completed with a failure; <code>false</code>
   *     otherwise
   */
  public default boolean isFailed() {
    return (getState() == State.FAILED);
  }

  /**
   * Checks if this task is in an unknown state.
   *
   * @return <code>true</code> if the task is in an unknown state, otherwise <code>false</code>
   */
  public default boolean isUnknown() {
    return (getState() == State.UNKNOWN);
  }

  /**
   * Gets the time when the first attempted task was queued.
   *
   * @return the time when the first attempted task was queued
   */
  public Instant getOriginalQueuedTime();

  /**
   * Gets the time when the task was most recently queued (in the case of being queued multiple
   * times). This will be the same time as reported by {@link #getOriginalQueuedTime()} if the task
   * has never been requeued after a failed attempt.
   *
   * @return the time when this task was queued
   */
  public Instant getQueuedTime();

  /**
   * Gets the total amount of time since this task has been created until it has completed
   * successfully or not.
   *
   * <p><i>Note:</i> The reported value will keep changing if this task is not completed.
   *
   * @return the total duration for this task
   */
  public Duration getDuration();

  /**
   * Gets the total amount of time this task has spent in the specified transitional state (see
   * {@link State#isTransitional()}).
   *
   * <p><i>Note:</i> The reported value will keep changing whenever this transformation is in the
   * specified state.
   *
   * @param state the transitional state for which to get the total amount of time spent in
   * @return the total duration spent in the specified state
   * @throws IllegalArgumentException if called with a final state
   */
  public Duration getDuration(State state);

  /**
   * Checks if this task is still locked by the current thread/worker. A task will automatically be
   * unlocked once one of {@link #unlock()}, {@link #complete()}, {@link #fail(ErrorCode)}, or
   * {@link #fail(ErrorCode, String)} has been called on it.
   *
   * @return <code>true</code> if the current thread/worker owns the lock to this task; <code>false
   *     </code> otherwise
   */
  public boolean isLocked();

  /**
   * Attempts to unlock this task. An unlocked task will be picked up by the next worker attempting
   * to poll for a task from the associated queue.
   *
   * @throws IllegalStateException if the task was already marked completed via one of {@link
   *     #complete()}, {@link #fail(ErrorCode)}, or {@link #fail(ErrorCode, String)}
   * @throws IllegalMonitorStateException if the current thread/worker doesn't own the lock to the
   *     task or if the task is not currently locked
   * @throws InterruptedException if the thread was interrupted while attempting to unlock the task
   * @throws E if an error occurred while performing the operation
   */
  public void unlock() throws InterruptedException, E;

  /**
   * Reports the successful completion of this task. The task will automatically be unlocked and
   * removed from the associated queue.
   *
   * @throws IllegalStateException if the task was already marked completed via one of {@link
   *     #complete()}, {@link #fail(ErrorCode)}, or {@link #fail(ErrorCode, String)}
   * @throws IllegalMonitorStateException if the current thread/worker doesn't own the lock to the
   *     task or if the task is not currently or no longer locked
   * @throws InterruptedException if the thread was interrupted while attempting to mark the task as
   *     completed
   * @throws E if an error occurred while performing the operation
   */
  public void complete() throws InterruptedException, E;

  /**
   * Reports the failed completion of this task. The task will automatically be unlocked and either
   * removed from or re-queued into the associated queue based on the error code provided. The task
   * will automatically be unlocked from the associated queue upon return.
   *
   * <p>Calling this method will block if the task needs to be re-queued based on the provided error
   * code and there is a need to wait for space to become available on the queue.
   *
   * @param reason the reason for the failure
   * @throws IllegalStateException if the task was already marked completed via one of {@link
   *     #complete()}, {@link #fail(ErrorCode)}, or {@link #fail(ErrorCode, String)}
   * @throws IllegalMonitorStateException if the current thread/worker doesn't own the lock to the
   *     task or if the task is not currently or no longer locked
   * @throws InterruptedException if the thread was interrupted while attempting to unlock the task
   * @throws E if an error occurred while performing the operation
   */
  public void fail(C reason) throws InterruptedException, E;

  /**
   * Reports the failed completion of this task. The task will automatically be unlocked and either
   * removed from or re-queued into the associated queue based on the error code provided. The task
   * will automatically be unlocked from the associated queue upon return.
   *
   * <p>Calling this method will block if the task needs to be re-queued based on the provided error
   * code and there is a need to wait for space to become available on the queue.
   *
   * @param reason the reason for the failure
   * @param message a message associated with the failure (used for traceability)
   * @throws IllegalStateException if the task was already marked completed via one of {@link
   *     #complete()}, {@link #fail(ErrorCode)}, or {@link #fail(ErrorCode, String)}
   * @throws IllegalMonitorStateException if the current thread/worker doesn't own the lock to the
   *     task or if the task is not currently or no longer locked
   * @throws InterruptedException if the thread was interrupted while attempting to unlock the task
   * @throws E if an error occurred while performing the operation
   */
  public void fail(C reason, String message) throws InterruptedException, E;

  /**
   * Limits the specified priority to a value in between 0 and 9.
   *
   * @param priority the priority to limit to the valid range
   * @return a corresponding priority that will be in the range 0 to 9
   */
  public static byte limit(byte priority) {
    return (byte) Math.min(Math.max(priority, Constants.MIN_PRIORITY), Constants.MAX_PRIORITY);
  }

  /** The various states a task can be in. */
  public enum State {
    /** Indicates the task is currently in queue awaiting to be picked up to be processed. */
    PENDING(false),

    /** Indicates the task is currently being processed. */
    PROCESSING(false),

    /** Indicates the task failed to be processed and will not be retried. */
    FAILED(true),

    /** Indicates the task processing has successfully completed. */
    SUCCESSFUL(true),

    /**
     * The unknown value is used for forward compatibility where the current code might not be able
     * to understand a new task state and would map this new state to <code>UNKNOWN</code> and most
     * likely ignore it.
     */
    UNKNOWN(false) {
      @Override
      public boolean isTransitional() {
        return false;
      }
    };

    private final boolean isFinal;

    private State(boolean isFinal) {
      this.isFinal = isFinal;
    }

    /**
     * Checks if this state represents a final state as opposed to a transitional state from which
     * we will transition out of later.
     *
     * @return <code>true</code> if this is a final state; <code>false</code> otherwise
     */
    public boolean isFinal() {
      return isFinal;
    }

    /**
     * Checks if this state represents a transitional state (i.e. a state we will transition out of
     * later) as opposed to a final state.
     *
     * @return <code>true</code> if this is a transitional state; <code>false</code> otherwise
     */
    public boolean isTransitional() {
      return !isFinal;
    }

    /**
     * Gets all final states.
     *
     * @return a stream of all final states
     */
    public static Stream<State> finals() {
      return Stream.of(State.values()).filter(State::isFinal);
    }

    /**
     * Gets all transitional states (i.e. those that are not final).
     *
     * @return a stream of all transitional states
     */
    public static Stream<State> transitionals() {
      return Stream.of(State.values()).filter(State::isTransitional);
    }

    /**
     * Reduces two states into one.
     *
     * <pre>
     * +------------+---------+------------+------------+------------+------------+
     * |            | PENDING | PROCESSING | FAILED     | SUCCESSFUL | UNKNOWN    |
     * +------------+---------+------------+------------+------------+------------+
     * | PENDING    | PENDING | PENDING    | PENDING    | PENDING    | PENDING    |
     * | PROCESSING | PENDING | PROCESSING | PROCESSING | PROCESSING | PROCESSING |
     * | FAILED     | PENDING | PROCESSING | FAILED     | FAILED     | UNKNOWN    |
     * | SUCCESSFUL | PENDING | PROCESSING | FAILED     | SUCCESSFUL | UNKNOWN    |
     * | UNKNOWN    | PENDING | PROCESSING | UNKNOWN    | UNKNOWN    | UNKNOWN    |
     * +------------+---------+------------+------------+------------+------------+
     * </pre>
     *
     * @param state the first state
     * @param state2 the second state
     * @return the aggregate state of the two given
     */
    public static State reduce(State state, State state2) {
      if ((state == State.PENDING) || (state2 == State.PENDING)) {
        return State.PENDING;
      } else if ((state == State.PROCESSING) || (state2 == State.PROCESSING)) {
        return State.PROCESSING;
      } else if ((state == State.UNKNOWN) || (state2 == State.UNKNOWN)) {
        return State.UNKNOWN;
      } else if ((state == State.SUCCESSFUL) && (state2 == State.SUCCESSFUL)) {
        return State.SUCCESSFUL;
      }
      return State.FAILED;
    }
  }

  /** Task-specific constants. */
  public final class Constants {
    /** Minimum task priority. */
    public static final byte MIN_PRIORITY = 0;

    /** Maximum task priority. */
    public static final byte MAX_PRIORITY = 9;

    private Constants() {} // prevent instantiation
  }
}

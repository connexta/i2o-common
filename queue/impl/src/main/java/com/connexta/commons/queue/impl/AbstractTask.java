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
package com.connexta.commons.queue.impl;

import com.connexta.commons.persistence.Persistable;
import com.connexta.commons.queue.ErrorCode;
import com.connexta.commons.queue.Task;
import com.connexta.commons.queue.TaskInfo;
import com.connexta.commons.queue.impl.persistence.AbstractTaskPojo;
import com.google.common.annotations.VisibleForTesting;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Base class for all tasks.
 *
 * @param <E> the base exception class for all exceptions that can be thrown from this API
 * @param <C> the enumeration for all error codes specified by this API
 * @param <I> the type of information associated with this task
 * @param <P> the type of pojo used to persist this task
 */
public abstract class AbstractTask<
        E extends Exception, C extends ErrorCode, I extends TaskInfo, P extends AbstractTaskPojo<P>>
    extends Persistable<P, E> implements Task<E, C> {
  private final Function<String, ? extends E> uvecreator;

  private volatile byte priority;
  private volatile Instant originalQueuedTime;

  private final Clock clock;

  private final AtomicInteger attempts = new AtomicInteger(1);
  private volatile Instant queuedTime;

  @SuppressWarnings("squid:S3077" /* acceptable to mark enum as volatile */)
  private volatile State state = State.PENDING;

  /**
   * The error code associated with the completion of this task. Will be <code>null</code> until it
   * fails at which point it will be used to tracked the reason for its failure. Will be reset to
   * <code>null</code> when requeued for reprocessing.
   */
  @SuppressWarnings("squid:S3077" /* acceptable to mark enum as volatile */)
  @Nullable
  private volatile C failureReason = null;

  @Nullable private volatile String failureMessage = null;

  /**
   * Thawed monotonic time in nanoseconds to calculate additional duration time for the current
   * state. It represents the time at which this object was reloaded from persistence or thawed from
   * a frozen state (i.e. persisted)
   *
   * <p><i>Note:</i> The delta between now and this time gets added to the duration that corresponds
   * to the current state and to the total one. In addition, this thawed time will be reset every
   * time the state is changed such that we can start calculating duration for the new state.
   */
  private volatile long thawedTime;

  // durations are all in nanoseconds
  private volatile Duration duration = Duration.ZERO;
  private Map<State, Duration> durations = new ConcurrentHashMap<>(12);

  /**
   * Creates a new task for the corresponding task information as it is being queued to a given
   * queue.
   *
   * @param persistableType a string representing the type of this object (used when generating
   *     exception or logs)
   * @param eclazz the base exception class for all exceptions that can be thrown from this API
   * @param iecreator a function to instantiate an error (typically a subclass of <code>E</code>)
   *     when an invalid field is uncovered during serialization or deserialization. The function is
   *     called with a message and an optional cause for the error (might be <code>null</code> if no
   *     cause is available)
   * @param uvecreator a function to instantiate an unsupported version error during
   *     deserialization. The function is called with a message
   * @param priority the priority for the task
   * @param clock the clock to use for retrieving wall and monotonic times
   */
  public AbstractTask(
      String persistableType,
      Class<E> eclazz,
      BiFunction<String, Throwable, ? extends E> iecreator,
      Function<String, ? extends E> uvecreator,
      byte priority,
      Clock clock) {
    super(persistableType, eclazz, iecreator);
    this.uvecreator = uvecreator;
    this.clock = clock;
    this.priority = Task.limit(priority);
    final Instant wallTime = Instant.ofEpochMilli(clock.wallTime());
    final long monotonicTime = clock.monotonicTime();

    this.originalQueuedTime = wallTime;
    this.queuedTime = wallTime;
    this.thawedTime = monotonicTime;
  }

  /**
   * Instantiates a new task corresponding to an object being reloaded from persistence in which
   * case the identifier can either be provided or <code>null</code> can be provided with the
   * expectation that the identifier and the rest of the task information will be restored as soon
   * as the subclass calls {@link #readFrom(P)} before returning any instances of the object.
   *
   * @param persistableType a string representing the type of this object (used when generating
   *     exception or logs)
   * @param eclazz the class of exception thrown from persistence operations
   * @param iecreator a function to instantiate an error (typically a subclass of <code>E</code>)
   *     when an invalid field is uncovered during serialization or deserialization. The function is
   *     called with a message and an optional cause for the error (might be <code>null</code> if no
   *     cause is available)
   * @param uvecreator a function to instantiate an unsupported version error during
   *     deserialization. The function is called with a message
   * @param clock the clock to use for retrieving wall and monotonic times
   */
  protected AbstractTask(
      String persistableType,
      Class<E> eclazz,
      BiFunction<String, Throwable, ? extends E> iecreator,
      Function<String, ? extends E> uvecreator,
      Clock clock) {
    super(persistableType, null, eclazz, iecreator);
    this.uvecreator = uvecreator;
    this.clock = clock;
  }

  /**
   * Gets the information for this task.
   *
   * @return the corresponding task info
   */
  public abstract I getInfo();

  @Override
  public byte getPriority() {
    return priority;
  }

  @Override
  public int getAttempts() {
    return attempts.get();
  }

  @Override
  public State getState() {
    return state;
  }

  /**
   * Gets the optional reason indicating why the task failed.
   *
   * <p><i>Note:</i> This reason is never persisted since a failed task is either final or retried.
   * In the former case, it wouldn't be persisted in the queue again. In the latter case it would be
   * put back in the queue with a {@link State#PENDING} for another attempt.
   *
   * @return the reason why the transformation failure or empty if the transformation completed
   *     successfully or if it is still pending or in progress
   */
  public Optional<C> getFailureReason() {
    return Optional.ofNullable(failureReason);
  }

  /**
   * Gets the optional message indicating why the task failed.
   *
   * <p><i>Note:</i> This message is never persisted since a failed task is either final or retried.
   * In the former case, it wouldn't be persisted in the queue again. In the latter case it would be
   * put back in the queue with a {@link State#PENDING} for another attempt.
   *
   * @return the message associated with the task failure or empty if the task completed
   *     successfully or if it is still pending or in progress
   */
  public Optional<String> getFailureMessage() {
    return Optional.ofNullable(failureMessage);
  }

  @Override
  public Instant getOriginalQueuedTime() {
    return originalQueuedTime;
  }

  @Override
  public Instant getQueuedTime() {
    return queuedTime;
  }

  @Override
  public Duration getDuration() {
    return getDuration(clock::monotonicTime);
  }

  @Override
  public Duration getDuration(State state) {
    org.apache.commons.lang3.Validate.isTrue(
        state.isTransitional(), "not a transitional state: %s", state);
    return getDuration(state, clock::monotonicTime);
  }

  @Override
  public boolean hasUnknowns() {
    return super.hasUnknowns() || (state == State.UNKNOWN);
  }

  @Override
  public int hashCode() {
    return hashCode0();
  }

  @Override
  public boolean equals(Object obj) {
    return equals0(obj);
  }

  @Override
  public String toString() {
    return String.format(
        "AbstractTask[id=%s, priority=%d, info=%s, originalQueuedTime=%s, attempts=%d, queuedTime=%s, state=%s, failureReason=%s, failureMessage=%s, duration=%s, durations=%s, thawedTime=%s]",
        getId(),
        priority,
        getInfo(),
        originalQueuedTime,
        attempts.get(),
        queuedTime,
        state,
        failureReason,
        failureMessage,
        duration,
        durations,
        thawedTime);
  }

  /**
   * Sets the state of this task and recompute the durations as of now.
   *
   * @param state the new state for this task
   * @param reason the reason for the failure or for changing back the state to pending or <code>
   *     null</code> if the new state doesn't represent a failure
   * @param message a message associated with the failure (used for traceability) or <code>null
   *     </code> if none provided
   */
  protected void setState(State state, @Nullable C reason, @Nullable String message) {
    if (isCompleted()) {
      return;
    }
    final long monotonicTime = clock.monotonicTime();
    final long d = monotonicTime - thawedTime;
    final State previous = this.state;

    this.thawedTime = monotonicTime;
    this.state = state;
    this.duration = duration.plusNanos(d);
    if (previous.isTransitional()) {
      durations.compute(previous, (s, pd) -> (pd != null) ? pd.plusNanos(d) : Duration.ofNanos(d));
    }
    if (state == State.FAILED) {
      this.failureReason = reason;
      this.failureMessage = message;
    } else if (state == State.PENDING) {
      if ((reason != null) && reason.isRetryable()) {
        // only increment attempt and update queued time if the state is changed back to pending
        // and a retryable error is associated with that change which means we are re-queuing it
        attempts.incrementAndGet();
        this.queuedTime = Instant.ofEpochMilli(clock.wallTime());
        this.failureReason = reason;
        this.failureMessage = message;
      } else {
        this.failureReason = null;
        this.failureMessage = null;
      }
    } else {
      this.failureReason = null;
      this.failureMessage = null;
    }
  }

  @Override
  protected P writeTo(P pojo) throws E {
    super.writeTo(pojo);
    setOrFailIfNull("originalQueuedTime", this::getOriginalQueuedTime, pojo::setOriginalQueuedTime);
    setOrFailIfNull("queuedTime", this::getQueuedTime, pojo::setQueuedTime);
    convertAndSetOrFailIfNull("state", this::getState, State::name, pojo::setState);
    // freeze time so we can serialize this task with durations calculated up to that time
    final long wallTime = clock.wallTime();
    final long monotonicTime = clock.monotonicTime();

    setOrFailIfNull("duration", () -> getDuration(() -> monotonicTime), pojo::setDuration);
    return pojo.setBaseVersion(AbstractTaskPojo.CURRENT_BASE_VERSION)
        .setPriority(priority)
        .setAttempts(attempts.get())
        .setDurations(getDurations(() -> monotonicTime))
        .setFrozenTime(wallTime);
  }

  @Override
  protected void readFrom(P pojo) throws E {
    super.readFrom(pojo);
    if (pojo.getBaseVersion() < AbstractTaskPojo.MINIMUM_BASE_VERSION) {
      throw fill(
          eclazz.cast(
              uvecreator
                  .apply(
                      "unsupported "
                          + persistableType
                          + " base version: "
                          + pojo.getBaseVersion()
                          + " for object: "
                          + getId())
                  .fillInStackTrace()));
    } // do support pojo.getBaseVersion() > CURRENT_BASE_VERSION for forward compatibility
    readFromCurrentOrFutureVersion(pojo);
  }

  /**
   * Gets an unmodifiable view of all the transitional state durations.
   *
   * @return an unmodifiable view of all transitional state durations
   */
  protected Map<State, Duration> getDurations() {
    return Collections.unmodifiableMap(durations);
  }

  /**
   * Gets the time in milliseconds from the epoch when this task was last thawed.
   *
   * @return the thawed time
   */
  protected long getThawedTime() {
    return thawedTime;
  }

  @VisibleForTesting
  void setPriority(byte priority) {
    this.priority = priority;
  }

  @VisibleForTesting
  void setOriginalQueuedTime(Instant originalQueuedTime) {
    this.originalQueuedTime = originalQueuedTime;
  }

  @VisibleForTesting
  void setAttempts(int attempts) {
    this.attempts.set(attempts);
  }

  @VisibleForTesting
  void setQueuedTime(Instant queuedTime) {
    this.queuedTime = queuedTime;
  }

  @VisibleForTesting
  void setState(State state) {
    this.state = state;
  }

  @VisibleForTesting
  void setFailureReason(@Nullable C failureReason) {
    this.failureReason = failureReason;
  }

  @VisibleForTesting
  void setFailureMessage(@Nullable String failureMessage) {
    this.failureMessage = failureMessage;
  }

  @VisibleForTesting
  void setThawedTime(long thawedTime) {
    this.thawedTime = thawedTime;
  }

  @VisibleForTesting
  void setDuration(Duration duration) {
    this.duration = duration;
  }

  @VisibleForTesting
  void setDurations(Map<State, Duration> durations) {
    this.durations = durations;
  }

  @VisibleForTesting
  int hashCode0() {
    return Objects.hash(
        super.hashCode(),
        priority,
        originalQueuedTime,
        getAttempts(),
        queuedTime,
        state,
        failureReason,
        failureMessage,
        thawedTime,
        duration,
        durations);
  }

  @VisibleForTesting
  boolean equals0(Object obj) {
    if (super.equals(obj) && (obj instanceof AbstractTask)) {
      final AbstractTask task = (AbstractTask) obj;

      return (priority == task.priority)
          && (getAttempts() == task.getAttempts())
          && (state == task.state)
          && (failureReason == task.failureReason)
          && (thawedTime == task.thawedTime)
          && Objects.equals(originalQueuedTime, task.originalQueuedTime)
          && Objects.equals(queuedTime, task.queuedTime)
          && Objects.equals(failureMessage, task.failureMessage)
          && Objects.equals(duration, task.duration)
          && Objects.equals(durations, task.durations);
    }
    return false;
  }

  private void readFromCurrentOrFutureVersion(P pojo) throws E {
    setPriority(Task.limit(pojo.getPriority()));
    setOrFailIfNull("originalQueuedTime", pojo::getOriginalQueuedTime, this::setOriginalQueuedTime);
    setAttempts(pojo.getAttempts());
    setOrFailIfNull("queuedTime", pojo::getQueuedTime, this::setQueuedTime);
    convertAndSetEnumValueOrFailIfNullOrEmpty(
        "state", State.class, State.UNKNOWN, pojo::getState, this::setState);
    final long wallTime = clock.wallTime();
    final long monotonicTime = clock.monotonicTime();
    // get the frozen time and re-calculate durations to include the time spent frozen
    final long frozenMillis =
        Math.max(wallTime - pojo.getFrozenTime(), 0L); // don't go back in time

    setOrFailIfNull("duration", pojo::getDuration, d -> setDuration(d, frozenMillis));
    setDurationsAndCheckForUnknown(pojo.getDurations(), frozenMillis);
    this.thawedTime = monotonicTime;
  }

  private Duration getDuration(LongSupplier monotonicTime) {
    final long t = thawedTime;
    final Duration d = duration;

    return isCompleted() ? d : d.plusNanos(monotonicTime.getAsLong() - t);
  }

  private void setDuration(Duration duration, long millis) {
    this.duration = getState().isTransitional() ? duration.plusMillis(millis) : duration;
  }

  private Duration getDuration(State state, LongSupplier monotonicTime) {
    final long t = thawedTime;
    final Duration d = durations.getOrDefault(state, Duration.ZERO);

    return (this.state == state) ? d.plusNanos(monotonicTime.getAsLong() - t) : d;
  }

  private Map<String, Duration> getDurations(LongSupplier monotonicTime) {
    return durations.keySet().stream()
        .collect(Collectors.toMap(State::name, s -> getDuration(s, monotonicTime)));
  }

  private void setDurationsAndCheckForUnknown(Map<String, Duration> durations, long millis) {
    this.durations =
        durations.entrySet().stream()
            .collect(Collectors.toConcurrentMap(AbstractTask::toState, Map.Entry::getValue));
    if (state.isTransitional()) {
      this.durations.computeIfPresent(state, (s, d) -> d.plusMillis(millis));
    }
    super.hasUnknowns |= this.durations.containsKey(State.UNKNOWN);
  }

  private static State toState(Map.Entry<String, Duration> entry) {
    try {
      return State.valueOf(entry.getKey());
    } catch (IllegalArgumentException iae) {
      return State.UNKNOWN;
    }
  }
}

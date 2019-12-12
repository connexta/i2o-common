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
package com.connexta.commons.queue.impl.persistence;

import com.connexta.commons.persistence.Pojo;
import com.connexta.commons.queue.Task;
import com.connexta.commons.queue.Task.State;
import com.connexta.commons.queue.impl.persistence.unknown.UnknownTaskPojo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * This class provides a base pojo implementation for a task capable of reloading all supported
 * fields for all supported versions from a Json string. It also provides the capability of
 * persisting back the fields based on the latest version format.
 *
 * @param <P> the type of pojo this is
 */
@JsonPropertyOrder({"clazz", "id", "base_version", "version", "priority"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(
    use = Id.NAME,
    include = As.PROPERTY,
    property = "clazz",
    defaultImpl = UnknownTaskPojo.class)
@JsonSubTypes(@Type(AbstractTaskPojo.class))
@JsonTypeName("base_task")
public abstract class AbstractTaskPojo<P extends AbstractTaskPojo<P>> extends Pojo<P> {
  /**
   * Current base version format.
   *
   * <p>Base version history:
   *
   * <ul>
   *   <li>1 - initial base version.
   * </ul>
   */
  public static final int CURRENT_BASE_VERSION = 1;

  /** The oldest base version supported by the current code (anything before that will fail). */
  public static final int MINIMUM_BASE_VERSION = 1;

  @JsonProperty("base_version")
  private int baseVersion;

  @JsonProperty("priority")
  private byte priority;

  @JsonProperty("original_queued_time")
  @Nullable
  private Instant originalQueuedTime;

  @JsonProperty("attempts")
  private int attempts;

  @JsonProperty("queued_time")
  @Nullable
  private Instant queuedTime;

  @JsonProperty("state")
  @Nullable
  private String state;

  @JsonProperty("frozen_time")
  private long frozenTime;

  @JsonProperty("duration")
  @Nullable
  private Duration duration;

  @JsonProperty("durations")
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
  private Map<String, Duration> durations = new HashMap<>(12);

  /**
   * Gets the serialized version for this base pojo.
   *
   * @return the base version for this pojo
   */
  public int getBaseVersion() {
    return baseVersion;
  }

  /**
   * Sets the serialized version for this base pojo.
   *
   * @param baseVersion the base version for this pojo
   * @return this for chaining
   */
  public P setBaseVersion(int baseVersion) {
    this.baseVersion = baseVersion;
    return (P) this;
  }

  /**
   * Gets the priority for this task, represented as a number from 9 being the highest level to 0
   * being the lowest.
   *
   * @return the priority for the task
   */
  public byte getPriority() {
    return priority;
  }

  /**
   * Sets the priority for this task, represented as a number from 9 being the highest level to 0
   * being the lowest.
   *
   * @param priority the priority for the task
   * @return this for chaining
   */
  public P setPriority(byte priority) {
    this.priority = priority;
    return (P) this;
  }

  /**
   * Gets the time when the first attempted task was queued.
   *
   * @return the time when the first attempted task was queued from the epoch
   */
  @Nullable
  public Instant getOriginalQueuedTime() {
    return originalQueuedTime;
  }

  /**
   * Sets the time when the first attempted task was queued.
   *
   * @param originalQueuedTime the time when the first attempted task was queued from the epoch
   * @return this for chaining
   */
  public P setOriginalQueuedTime(@Nullable Instant originalQueuedTime) {
    this.originalQueuedTime = originalQueuedTime;
    return (P) this;
  }

  /**
   * Gets the number of times this particular task has been attempted. When a task fails for a
   * reason that doesn't preclude it from being re-tried and it gets re-queued by the worker, its
   * total attempts counter will automatically be incremented as it is re-added to the associated
   * queue for later reprocessing.
   *
   * @return the total number of times this task has been attempted so far
   */
  public int getAttempts() {
    return attempts;
  }

  /**
   * Sets the number of times this particular task has been attempted. When a task fails for a
   * reason that doesn't preclude it from being re-tried and it gets re-queued by the worker, its
   * total attempts counter will automatically be incremented as it is re-added to the associated
   * queue for later reprocessing.
   *
   * @param attempts the total number of times this task has been attempted so far
   * @return this for chaining
   */
  public P setAttempts(int attempts) {
    this.attempts = attempts;
    return (P) this;
  }

  /**
   * Gets the time when the task was queued. This will be the same time as reported by {@link
   * #getOriginalQueuedTime()} if the task has never been requeued after a failed attempt.
   *
   * @return the time when this task was queued from the epoch
   */
  @Nullable
  public Instant getQueuedTime() {
    return queuedTime;
  }

  /**
   * Sets the time when the task was queued. This will be the same time as reported by {@link
   * #getOriginalQueuedTime()} if the task has never been requeued after a failed attempt.
   *
   * @param queuedTime the time when this task was queued from the epoch
   * @return this for chaining
   */
  public P setQueuedTime(@Nullable Instant queuedTime) {
    this.queuedTime = queuedTime;
    return (P) this;
  }

  /**
   * Gets the current state of the task.
   *
   * @return the current task state
   */
  @Nullable
  public String getState() {
    return state;
  }

  /**
   * Sets the current state of the task.
   *
   * @param state the current task state
   * @return this for chaining
   */
  public P setState(@Nullable String state) {
    this.state = state;
    return (P) this;
  }

  /**
   * Sets the current state of the task.
   *
   * @param state the current task state
   * @return this for chaining
   */
  public P setState(@Nullable Task.State state) {
    this.state = (state != null) ? state.name() : null;
    return (P) this;
  }

  /**
   * Gets the time when the pojo was created. At that time it is expected that durations would have
   * been recomputed to accumulate time up to that moment.
   *
   * @return the time in milliseconds when this pojo was created and durations were re-computed
   */
  public long getFrozenTime() {
    return frozenTime;
  }

  /**
   * Sets the time when the pojo was created. At that time it is expected that durations would have
   * been recomputed to accumulate time up to that moment.
   *
   * @param frozenTime the time in milliseconds when this pojo was created and durations were
   *     re-computed
   * @return this for chaining
   */
  public P setFrozenTime(long frozenTime) {
    this.frozenTime = frozenTime;
    return (P) this;
  }

  /**
   * Gets the total amount of time since this task has been created until it has completed
   * successfully or not.
   *
   * @return the total duration for this task
   */
  @Nullable
  public Duration getDuration() {
    return duration;
  }

  /**
   * Sets the total amount of time since this task has been created until it has completed
   * successfully or not.
   *
   * @param duration the total duration for this task
   * @return this for chaining
   */
  public P setDuration(@Nullable Duration duration) {
    this.duration = duration;
    return (P) this;
  }

  /**
   * Gets the set of durations on a per-transitional-state basis for this task as calculated when
   * time was frozen.
   *
   * @return the set of durations on a per-transitional state basis for this task
   */
  public Map<String, Duration> getDurations() {
    return durations;
  }

  /**
   * Gets the set of durations on a per-transitional-state basis for this task as calculated when
   * time was frozen.
   *
   * @return a stream of all durations on a per-transitional-state basis for this task
   */
  public Stream<Entry<String, Duration>> durations() {
    return durations.entrySet().stream();
  }

  /**
   * Sets the set of durations on a per-transitional-state basis for this task as calculated when
   * time was frozen.
   *
   * @param durations the set of durations on a per-transitional-state basis for this task or <code>
   *     null</code> if none defined
   * @return this for chaining
   */
  public P setDurations(@Nullable Map<String, Duration> durations) {
    this.durations = (durations != null) ? durations : new HashMap<>(12);
    return (P) this;
  }

  /**
   * Sets the the duration for a specific transitional state for this task as calculated when time
   * was frozen.
   *
   * @param state the transitional state for which to set the duration
   * @param duration the duration for the specified transitional state for this task
   * @return this for chaining
   */
  public P setDuration(String state, Duration duration) {
    durations.put(state, duration);
    return (P) this;
  }

  /**
   * Sets the the duration for a specific transitional state for this task as calculated when time
   * was frozen.
   *
   * @param state the transitional state for which to set the duration
   * @param duration the duration for the specified transitional state for this task
   * @return this for chaining
   */
  public P setDuration(State state, Duration duration) {
    return setDuration(state.name(), duration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        baseVersion,
        priority,
        originalQueuedTime,
        attempts,
        queuedTime,
        state,
        frozenTime,
        duration,
        durations);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof AbstractTaskPojo)) {
      final AbstractTaskPojo pojo = (AbstractTaskPojo) obj;

      return (baseVersion == pojo.baseVersion)
          && (priority == pojo.priority)
          && (attempts == pojo.attempts)
          && (frozenTime == pojo.frozenTime)
          && Objects.equals(originalQueuedTime, pojo.originalQueuedTime)
          && Objects.equals(queuedTime, pojo.queuedTime)
          && Objects.equals(state, pojo.state)
          && Objects.equals(duration, pojo.duration)
          && Objects.equals(durations, pojo.durations);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "AbstractTaskPojo[id=%s, baseVersion=%d, version=%d, priority=%d, originalQueuedTime=%s, attempts=%d, queuedTime=%s, state=%s, frozenTime=%d, duration=%s, durations=%s]",
        getId(),
        baseVersion,
        getVersion(),
        priority,
        originalQueuedTime,
        attempts,
        queuedTime,
        state,
        frozenTime,
        duration,
        durations);
  }
}

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

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * Interface for all type of queues from which tasks can be retrieved.
 *
 * @param <E> the base exception class for all exceptions that can be thrown from this API
 * @param <C> the enumeration for all error codes specified by this API
 * @param <T> the type of task that can be retrieved from this queue
 */
public interface Queue<E extends Exception, C extends ErrorCode, T extends Task<E, C>> {
  /** This class represents a unique identifier for queues. */
  public static final class Id implements Comparable<Id> {
    /**
     * Creates a new queue identifier.
     *
     * @param taskClass the class of tasks for the queue being referenced
     * @param subId the unique id within the scope of the task class
     * @return the corresponding queue identifier
     */
    public static Id of(Class<? extends Task> taskClass, String subId) {
      return new Id(taskClass, subId);
    }

    private final Class<? extends Task> taskClass;
    private final String subId;

    private Id(Class<? extends Task> taskClass, String subId) {
      this.taskClass = taskClass;
      this.subId = subId;
    }

    /**
     * Gets the class for the tasks managed by the queue being referenced.
     *
     * @return the task class for the queue
     */
    public Class<? extends Task> getTaskClass() {
      return taskClass;
    }

    /**
     * Gets the unique identifier within the scope of the task class for the queue being referenced
     *
     * @return the unique id for the queue within the scope of the task class
     */
    public String getId() {
      return subId;
    }

    @Override
    public int hashCode() {
      return Objects.hash(taskClass, subId);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      } else if (obj instanceof Id) {
        final Id i = (Id) obj;

        return taskClass.equals(i.taskClass) && subId.equals(i.subId);
      }
      return false;
    }

    @Override
    public int compareTo(Id id) {
      final int c = subId.compareTo(id.subId);

      if (c != 0) {
        return c;
      }
      return taskClass.getName().compareTo(id.taskClass.getName());
    }

    @Override
    public String toString() {
      return subId + '@' + taskClass.getCanonicalName();
    }
  }

  /**
   * Retrieves the next available highest priority task, waiting if necessary until one becomes
   * available. The returned task is considered locked by the current thread/worker.
   *
   * @return the next available task with the highest priority from this queue
   * @throws InterruptedException if the current thread was interrupted while waiting for a task to
   *     be returned
   * @throws E if an error occurred while performing the operation
   */
  public T take() throws InterruptedException, E;

  /**
   * Retrieves the next available highest priority task, waiting up to the specified wait time if
   * necessary for one to become available. The returned task is considered locked by the current
   * thread/worker.
   *
   * @param timeout how long to wait before giving up, in units of <code>unit</code>
   * @param unit a {@code TimeUnit} determining how to interpret the <code>timeout</code> parameter
   * @return the next available task with the highest priority from this queue or <code>null</code>
   *     if the specified amount time elapsed before a task is available
   * @throws InterruptedException if the current thread was interrupted while waiting for a task to
   *     be returned
   * @throws E if an error occurred while performing the operation
   */
  @Nullable
  public T poll(long timeout, TimeUnit unit) throws InterruptedException, E;
}

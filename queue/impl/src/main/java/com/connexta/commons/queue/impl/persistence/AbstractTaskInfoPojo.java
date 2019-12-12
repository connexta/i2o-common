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
import com.connexta.commons.queue.impl.persistence.unknown.UnknownTaskInfoPojo;
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
import java.util.Objects;

/**
 * This class provides a base pojo implementation for a task info object capable of reloading all
 * supported fields for all supported versions from a Json string. It also provides the capability
 * of persisting back the fields based on the latest version format.
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
    defaultImpl = UnknownTaskInfoPojo.class)
@JsonSubTypes(@Type(AbstractTaskInfoPojo.class))
@JsonTypeName("base_task_info")
public abstract class AbstractTaskInfoPojo<P extends AbstractTaskInfoPojo<P>> extends Pojo<P> {
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

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), baseVersion, priority);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof AbstractTaskInfoPojo)) {
      final AbstractTaskInfoPojo pojo = (AbstractTaskInfoPojo) obj;

      return (baseVersion == pojo.baseVersion) && (priority == pojo.priority);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "AbstractTaskInfoPojo[id=%s, baseVersion=%d, version=%d, priority=%d]",
        getId(), baseVersion, getVersion(), priority);
  }
}

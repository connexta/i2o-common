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
package com.connexta.commons.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;

/**
 * This class provides a base pojo implementation capable of reloading common supported fields for
 * all supported versions from a persistent storage. It also provides the capability of persisting
 * back those common fields based on the latest version format.
 *
 * @param <P> the type of pojo this is
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"id", "version"})
@JsonInclude(Include.NON_NULL)
public abstract class Pojo<P extends Pojo<P>> {
  @Id
  @Indexed(name = "id", required = true)
  @JsonProperty("id")
  @Nullable
  private UUID id;

  @Indexed(name = "version", required = true)
  @JsonProperty("version")
  private int version;

  /** Instantiates a blank pojo. */
  protected Pojo() {}

  /**
   * Gets the identifier for this pojo.
   *
   * @return the pojo id
   */
  @Nullable
  public UUID getId() {
    return id;
  }

  /**
   * Sets the identifier for this pojo.
   *
   * @param id the id for this pojo
   * @return this for chaining
   */
  public P setId(UUID id) {
    this.id = id;
    return (P) this;
  }

  /**
   * Gets the serialized version for this pojo.
   *
   * @return the version for this pojo
   */
  public int getVersion() {
    return version;
  }

  /**
   * Sets the serialized version for this pojo.
   *
   * @param version the pojo version
   * @return this for chaining
   */
  public P setVersion(int version) {
    this.version = version;
    return (P) this;
  }

  @Override
  public int hashCode() {
    return hashCode0();
  }

  @Override
  public boolean equals(Object obj) {
    return equals0(obj);
  }

  @VisibleForTesting
  int hashCode0() {
    return Objects.hash(id, version);
  }

  @VisibleForTesting
  boolean equals0(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Pojo) {
      final Pojo pojo = (Pojo) obj;

      return (version == pojo.version) && Objects.equals(id, pojo.id);
    }
    return false;
  }
}

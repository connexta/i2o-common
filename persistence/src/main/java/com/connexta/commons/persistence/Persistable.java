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

import com.connexta.commons.function.ThrowingConsumer;
import com.connexta.commons.function.ThrowingFunction;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * This class defines a base class for all objects that can be persisted to supported stores. It
 * provides methods that should be implemented by all these objects to perform the serialization and
 * deserialization of its attributes to pojos.
 *
 * @param <P> the type of pojo associated with this persistable
 * @param <E> the type of exceptions thrown from persistence operations
 */
public abstract class Persistable<P extends Pojo<P>, E extends Exception> {
  private static final String EMPTY = "empty";
  private static final String INVALID = "invalid";
  private static final String MISSING = "missing";
  private static final String UNKNOWN = "unknown";

  private UUID id;

  protected final Class<E> eclazz;

  protected final BiFunction<String, Throwable, ? extends E> iecreator;

  /** A string representing the type of this object. Used when generating exception or logs. */
  protected final String persistableType;

  protected boolean hasUnknowns = false;

  /**
   * Instantiates a new persistable corresponding to a new object that do not yet exist in the
   * persistence store.
   *
   * @param persistableType a string representing the type of this object (used when generating
   *     exception or logs)
   * @param eclazz the class of exception thrown from persistence operations
   * @param iecreator a function to instantiate an error (typically a subclass of <code>E</code>)
   *     when an invalid field is uncovered during serialization or deserialization. The function is
   *     called with a message and an optional cause for the error (might be <code>null</code> if no
   *     cause is available)
   */
  protected Persistable(
      String persistableType,
      Class<E> eclazz,
      BiFunction<String, Throwable, ? extends E> iecreator) {
    this(persistableType, UUID.randomUUID(), eclazz, iecreator);
  }

  /**
   * Instantiates a new persistable corresponding to an object being reloaded from persistence in
   * which case the identifier can either be provided or <code>null</code> can be provided with the
   * expectation that the identifier will be restored as soon as the subclass calls {@link
   * #readFrom(Pojo)}.
   *
   * <p><i>Note:</i> It is the responsibility of the derived class which calls this constructor with
   * a <code>null</code> identifier to ensure that the identifier is restored or set before
   * returning any instances of the object.
   *
   * @param persistableType a string representing the type of this object (used when generating
   *     exception or logs)
   * @param id the previously generated identifier for this object or <code>null</code> if it is
   *     expected to be restored later when {@link #readFrom(Pojo)} is called by the subclass
   * @param eclazz the class of exception thrown from persistence operations
   * @param iecreator a function to instantiate an error (typically a subclass of <code>E</code>)
   *     when an invalid field is uncovered during serialization or deserialization. The function is
   *     called with a message and an optional cause for the error (might be <code>null</code> if no
   *     cause is available)
   */
  protected Persistable(
      String persistableType,
      @Nullable UUID id,
      Class<E> eclazz,
      BiFunction<String, Throwable, ? extends E> iecreator) {
    this.persistableType = persistableType;
    this.eclazz = eclazz;
    this.iecreator = iecreator;
    this.id = id;
  }

  /**
   * Gets the identifier of the persistable object.
   *
   * @return The ID of the object
   */
  public UUID getId() {
    return id;
  }

  /**
   * Checks if this persistable object contains unknown information.
   *
   * <p><i>Note:</i> The default implementation provided here will always return <code>false</code>.
   * Subclasses should override this method as required.
   *
   * @return <code>true</code> if it contains unknown info; <code>false</code> otherwise
   */
  public boolean hasUnknowns() {
    return hasUnknowns;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Persistable<?, ?>) {
      final Persistable<?, ?> persistable = (Persistable<?, ?>) obj;

      return Objects.equals(id, persistable.id);
    }
    return false;
  }

  /**
   * Serializes this object to the specified pojo that can be serialized to persistence. Any
   * implementation of this method in a subclass should first make a call to the super version of
   * this method before serializing its own properties.
   *
   * @param pojo the pojo to serialize this object to
   * @return <code>pojo</code> for chaining
   * @throws E if an error occurs while serializing this object
   */
  protected P writeTo(P pojo) throws E {
    if (hasUnknowns()) { // cannot serialize if it contains unknowns
      throw fill(eclazz.cast(newInvalidError(Persistable.UNKNOWN, null, null).fillInStackTrace()));
    }
    setOrFailIfNull("id", this::getId, pojo::setId);
    return pojo;
  }

  /**
   * Deserializes the specified pojo into this persistable object. This method is responsible for
   * performing any required conversion to the latest version. Any implementation of this method in
   * a subclass should first make a call to the super version before performing its own
   * functionality.
   *
   * @param pojo the pojo to deserialize
   * @throws E if an error occurs while deserializing the pojo object
   */
  protected void readFrom(P pojo) throws E {
    this.hasUnknowns = pojo instanceof UnknownPojo; // reset the unknown flag
    setOrFailIfNull("id", pojo::getId, this::setId);
  }

  /**
   * Called by the methods below to fill-in additional information on the provided persistence
   * exception.
   *
   * <p><i>Note:</i> Subclasses should override this method in order to fill-in information about
   * themselves on the provided exception. By default this method does nothing.
   *
   * @param exception the exception to fill-in additional info on
   * @return <code>exception</code> for chaining
   */
  protected E fill(E exception) {
    return exception;
  }

  /**
   * Useful method that can be used to convert the value of a field if it is not <code>null</code>
   * before setting it in a given destination. If it is <code>null</code> than <code>null</code> is
   * set and no conversion happens. The destination's corresponding field would be set accordingly.
   * The pojo field's value is retrieved using a consumer (e.g. <code>pojo::getName</code>) and set
   * in the destination using a supplier (e.g. <code>this::setName</code>).
   *
   * @param <S> the supplied type
   * @param <C> the consumed type
   * @param field the name of the field being checked for presence (used in the exception message)
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param converter a converter to convert from the field's value to the one set in the
   *     destination
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @throws E if the field's value as supplied by <code>supplier</code> cannot be converted or be
   *     set
   */
  protected final <S, C> void convertAndSet(
      String field,
      Supplier<? extends S> supplier,
      ThrowingFunction<? super S, ? extends C, ? extends Exception> converter,
      ThrowingConsumer<? super C, ? extends E> consumer)
      throws E {
    final S s = supplier.get();

    try {
      consumer.accept((s != null) ? converter.apply(s) : null);
    } catch (Exception e) {
      if (eclazz.isInstance(e)) {
        throw fill(eclazz.cast(e));
      }
      throw fill(eclazz.cast(newInvalidError(Persistable.INVALID, field, e).fillInStackTrace()));
    }
  }

  /**
   * Useful method that can be used to validate if the value of a field is not <code>null</code>
   * before setting it in a given destination. If it is <code>null</code> than an exception is
   * thrown otherwise the destination's corresponding field would be set accordingly. The field's
   * value is retrieved using a consumer (e.g. <code>pojo::getName</code>) and set in the
   * destination using a supplier (e.g. <code>this::setName</code>).
   *
   * @param <F> the field's type
   * @param field the name of the field being checked for presence (used in the exception message)
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @throws E if the field's value as supplied by <code>supplier</code> is <code>null</code> or
   *     cannot be set
   */
  protected final <F> void setOrFailIfNull(
      String field,
      Supplier<? extends F> supplier,
      ThrowingConsumer<? super F, ? extends E> consumer)
      throws E {
    consumer.accept(validateNotNull(field, supplier.get()));
  }

  /**
   * Useful method that can be used to validate if the value of a field is not <code>null</code>
   * before converting and setting it in a given destination. If it is <code>null</code> than an
   * exception is thrown otherwise the destination's corresponding field would be set accordingly.
   * The field's value is retrieved using a consumer (e.g. <code>pojo::getName</code>) then
   * converted before finally being set in the destination using a supplier (e.g. <code>
   * this::setName</code>).
   *
   * @param <F> the field's type
   * @param <P> the consumed type
   * @param field the name of the field being checked for presence (used in the exception message)
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param converter a converter to convert from the field's value to the one set in the
   *     destination
   * @param consumer a consumer capable of updating the destination with the converted field's value
   *     if it is defined
   * @throws E if the field's value as supplied by <code>supplier</code> is <code>null</code> or
   *     failed to be converted or set
   */
  protected final <F, P> void convertAndSetOrFailIfNull(
      String field,
      Supplier<? extends F> supplier,
      ThrowingFunction<? super F, ? extends P, ? extends Exception> converter,
      ThrowingConsumer<? super P, ? extends E> consumer)
      throws E {
    try {
      consumer.accept(converter.apply(validateNotNull(field, supplier.get())));
    } catch (Exception e) {
      if (eclazz.isInstance(e)) {
        throw fill(eclazz.cast(e));
      }
      throw fill(eclazz.cast(newInvalidError(Persistable.INVALID, field, e).fillInStackTrace()));
    }
  }

  /**
   * Useful method that can be used to validate if the value of a field is not <code>null</code> and
   * not empty before setting it in a given destination. If it is <code>null</code> or empty than an
   * exception is thrown otherwise the destination's corresponding field would be set accordingly.
   * The field's value is retrieved using a consumer (e.g. <code>pojo::getName</code>) and set in
   * the destination using a supplier (e.g. <code>this::setName</code>).
   *
   * @param field the name of the field being checked for presence (used in the exception message)
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @throws E if the field's value as supplied by <code>supplier</code> is <code>null</code> or
   *     empty or cannot be set
   */
  protected final void setOrFailIfNullOrEmpty(
      String field, Supplier<String> supplier, ThrowingConsumer<String, ? extends E> consumer)
      throws E {
    consumer.accept(validateNotNullAndNotEmpty(field, supplier.get()));
  }

  /**
   * Useful method that can be used to validate if the value of a field is not <code>null</code> and
   * not empty before converting and setting it in a given destination. If it is <code>null
   * </code> or empty than an exception is thrown otherwise the destination's corresponding field
   * would be set accordingly. The pojo field's value is retrieved using a consumer (e.g. <code>
   * pojo::getName</code>) and set in the destination using a supplier (e.g. <code>this::setName
   * </code>).
   *
   * @param <F> the consumed type
   * @param field the name of the field being checked for presence (used in the exception message)
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param converter a converter to convert from the field's value to the one set in the
   *     destination
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @throws E if the field's value as supplied by <code>supplier</code> is <code>null</code> or
   *     empty or cannot be converted or set
   */
  protected final <F> void convertAndSetOrFailIfNullOrEmpty(
      String field,
      Supplier<String> supplier,
      ThrowingFunction<String, ? extends F, ? extends Exception> converter,
      ThrowingConsumer<? super F, ? extends E> consumer)
      throws E {
    try {
      consumer.accept(converter.apply(validateNotNullAndNotEmpty(field, supplier.get())));
    } catch (Exception e) {
      if (eclazz.isInstance(e)) {
        throw fill(eclazz.cast(e));
      }
      throw fill(eclazz.cast(newInvalidError(Persistable.INVALID, field, e).fillInStackTrace()));
    }
  }

  /**
   * Useful method that can be used to validate if the value of an enum field is not <code>null
   * </code> or the specified <code>unknown</code> value before converting and setting it in a given
   * destination. If it is <code>null</code> or equal to the <code>unknown</code> value than an
   * exception is thrown otherwise the destination's corresponding field would be set accordingly.
   * The field's value is retrieved using a consumer (e.g. <code>pojo::getName</code>) then
   * converted before finally being set in the destination using a supplier (e.g. <code>
   * this::setName</code>).
   *
   * @param <F> the field's enum type
   * @param field the name of the field being checked for presence (used in the exception message)
   * @param unknown the enumeration value to be considered as an unknown value that shouldn't be
   *     converted
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param consumer a consumer capable of updating the destination with the converted field's value
   *     if it is defined
   * @throws E if the field's value as supplied by <code>supplier</code> is <code>null</code> or
   *     failed to be converted or set
   */
  protected final <F extends Enum<F>> void convertAndSetEnumValueOrFailIfNullOrUnknown(
      String field,
      F unknown,
      Supplier<? extends F> supplier,
      ThrowingConsumer<String, ? extends E> consumer)
      throws E {
    final F e = supplier.get();

    if (e == unknown) {
      throw fill(eclazz.cast(newInvalidError(Persistable.UNKNOWN, field, null).fillInStackTrace()));
    }
    consumer.accept(validateNotNull(field, e).name());
  }

  /**
   * Useful method that can be used to validate that the value of an enum field is defined as a
   * valid string representation of an enumeration value before setting it in a given destination.
   * If it is not a valid value, then <code>unknown</code> is used as the value to set in the
   * destination. Otherwise, the corresponding enum value is set accordingly. The field's value is
   * retrieved using a consumer (e.g. <code>pojo::getType</code>) and set in the destination using a
   * supplier (e.g. <code>this::setType</code>).
   *
   * @param <F> the field's enum type
   * @param field the name of the field being checked for presence (used in the exception message)
   * @param clazz the field's enumeration class to convert to
   * @param unknown the enumeration value to be used when the corresponding field's string value
   *     doesn't match any of the defined values
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @throws E if the field's value as supplied by <code>supplier</code> is <code>null</code> or
   *     empty or cannot be set
   */
  protected final <F extends Enum<F>> void convertAndSetEnumValueOrFailIfNullOrEmpty(
      String field,
      Class<F> clazz,
      F unknown,
      Supplier<String> supplier,
      ThrowingConsumer<F, ? extends E> consumer)
      throws E {
    try {
      consumer.accept(Enum.valueOf(clazz, validateNotNullAndNotEmpty(field, supplier.get())));
      return;
    } catch (IllegalArgumentException e) { // ignore
    }
    consumer.accept(unknown);
  }

  /**
   * Useful method that can be used to validate that the value of an enum field is defined as a
   * valid string representation of an enumeration value before setting it a given destination. If
   * it is not a valid value, then <code>unknown</code> is used as the value to set in this object.
   * Otherwise, the corresponding enum value is set accordingly. The field's value is retrieved
   * using a consumer (e.g. <code>pojo::getType</code>) and set in the destination using a supplier
   * (e.g. <code>this::setType</code>).
   *
   * @param <F> the field's enum type
   * @param clazz the field's enumeration class to convert to
   * @param unknown the enumeration value to be used when the corresponding field's string value
   *     doesn't match any of the defined values or is <code>null</code>
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @throws E if a failure occurs while setting the new value
   */
  protected final <F extends Enum<F>> void convertAndSetEnumValue(
      Class<F> clazz,
      F unknown,
      Supplier<String> supplier,
      ThrowingConsumer<F, ? extends E> consumer)
      throws E {
    convertAndSetEnumValue(clazz, unknown, unknown, supplier, consumer);
  }

  /**
   * Useful method that can be used to validate that the value of an enum field is defined as a
   * valid string representation of an enumeration value before setting it in a given destination.
   * If it is not a valid value, then <code>unknown</code> is used as the value to set in the
   * destination. Otherwise, the corresponding enum value is set accordingly. The field's value is
   * retrieved using a consumer (e.g. <code>pojo::getType</code>) and set in the destination using a
   * supplier (e.g. <code>this::setType</code>).
   *
   * @param <F> the field's enum type
   * @param clazz the field's enumeration class to convert to
   * @param ifNull the enumeration value to be used when the corresponding field's string value is
   *     <code>null</code> (<code>null</code> can be passed which means <code>null</code> will be
   *     set in the field's value)
   * @param unknown the enumeration value to be used when the corresponding field's string value
   *     doesn't match any of the defined values (or is empty)
   * @param supplier a supplier capable of retrieving the current value for the field
   * @param consumer a consumer capable of updating the destination with the field's value if it is
   *     defined
   * @throws E if a failure occurs while setting the new value
   */
  protected final <F extends Enum<F>> void convertAndSetEnumValue(
      Class<F> clazz,
      @Nullable F ifNull,
      F unknown,
      Supplier<String> supplier,
      ThrowingConsumer<F, ? extends E> consumer)
      throws E {
    final String value = supplier.get();

    if (value != null) {
      try {
        consumer.accept(Enum.valueOf(clazz, value));
        return;
      } catch (IllegalArgumentException e) { // ignore
      }
      consumer.accept(unknown);
    } else {
      consumer.accept(ifNull);
    }
  }

  /**
   * Validates the specified field's value cannot be <code>null</code>.
   *
   * @param field the name of the field being checked (used in the exception message)
   * @param value the field value to verify
   * @param <F> the type for the field value
   * @return <code>value</code> for chaining
   * @throws E if <code>value</code> is <code>null</code>
   */
  protected final <F> F validateNotNull(String field, @Nullable F value) throws E {
    if (value == null) {
      throw fill(eclazz.cast(newInvalidError(Persistable.MISSING, field, null).fillInStackTrace()));
    }
    return value;
  }

  /**
   * Validates the specified field's value cannot be <code>null</code> or empty.
   *
   * @param field the name of the field being checked (used in the exception message)
   * @param value the field value to verify
   * @return <code>value</code> for chaining
   * @throws E if <code>value</code> is <code>null</code> or empty
   */
  @SuppressWarnings("squid:S2259" /* value is verified first via validateNotNull() */)
  protected final String validateNotNullAndNotEmpty(String field, @Nullable String value) throws E {
    validateNotNull(field, value);
    if (value.isEmpty()) {
      throw fill(eclazz.cast(newInvalidError(Persistable.EMPTY, field, null).fillInStackTrace()));
    }
    return value;
  }

  @VisibleForTesting
  protected void setId(UUID id) {
    this.id = id;
  }

  @VisibleForTesting
  String getPersistableType() {
    return persistableType;
  }

  private E newInvalidError(String what, @Nullable String field, @Nullable Throwable cause) {
    final String f = (field != null) ? (" " + field) : "";

    if (id == null) {
      return iecreator.apply(what + " " + persistableType + f, cause);
    }
    return iecreator.apply(what + " " + persistableType + f + " for object: " + id, cause);
  }
}

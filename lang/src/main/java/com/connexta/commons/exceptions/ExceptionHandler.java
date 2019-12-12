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
package com.connexta.commons.exceptions;

import com.connexta.commons.function.ThrowingConsumer;
import com.connexta.commons.function.ThrowingFunction;
import com.connexta.commons.function.ThrowingRunnable;
import com.connexta.commons.function.ThrowingSupplier;
import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class provides a neat way for wrapping code that declares they throw checked exceptions from
 * within Java streaming or mapping methods where checked exceptions are not allowed. Once the code
 * used is wrapped using one of the <code>wrap()</code> it can then be unwrap back to the original
 * checked exception outside of the streaming chain.
 */
public final class ExceptionHandler {
  /**
   * Provides a function that wraps around another one where checked exceptions will be wrapped in
   * such a way that they can later be unwrapped using {@link #unwrap(Class, Supplier)}.
   *
   * <p><i>Note:</i> Only checked exceptions are wrapped. Runtime exceptions and errors will
   * continue to bubble out as normal.
   *
   * @param <T> the type of parameter for the function
   * @param <R> the type for the result from the function
   * @param <E> the type of exceptions that can be thrown by the operation
   * @param function the function to invoke an wrap checked exceptions from
   * @return a function that will wrap all checked exceptions from the provided function
   */
  public static <T, R, E extends Exception> Function<T, R> wrap(
      ThrowingFunction<T, R, E> function) {
    return p -> {
      try {
        return function.apply(p);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new WrappedException(e);
      }
    };
  }

  /**
   * Provides a supplier that wraps around another one where checked exceptions will be wrapped in
   * such a way that they can later be unwrapped using {@link #unwrap(Class, Supplier)}.
   *
   * <p><i>Note:</i> Only checked exceptions are wrapped. Runtime exceptions and errors will
   * continue to bubble out as normal.
   *
   * @param <R> the type for the result from the supplier
   * @param <E> the type of exceptions that can be thrown by the operation
   * @param supplier the supplier to invoke an wrap checked exceptions from
   * @return a supplier that will wrap all checked exceptions from the provided supplier
   */
  public static <R, E extends Exception> Supplier<R> wrap(ThrowingSupplier<R, E> supplier) {
    return () -> {
      try {
        return supplier.get();
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new WrappedException(e);
      }
    };
  }

  /**
   * Provides a consumer that wraps around another one where checked exceptions will be wrapped in
   * such a way that they can later be unwrapped using {@link #unwrap(Class, Runnable)}.
   *
   * <p><i>Note:</i> Only checked exceptions are wrapped. Runtime exceptions and errors will
   * continue to bubble out as normal.
   *
   * @param <T> the type for the value passed to the consumer
   * @param <E> the type of exceptions that can be thrown by the operation
   * @param consumer the consumer to invoke an wrap checked exceptions from
   * @return a consumer that will wrap all checked exceptions from the provided consumer
   */
  public static <T, E extends Exception> Consumer<T> wrap(ThrowingConsumer<T, E> consumer) {
    return t -> {
      try {
        consumer.accept(t);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new WrappedException(e);
      }
    };
  }

  /**
   * Provides a runnable that wraps around another one where checked exceptions will be wrapped in
   * such a way that they can later be unwrapped using {@link #unwrap(Class, Runnable)}.
   *
   * <p><i>Note:</i> Only checked exceptions are wrapped. Runtime exceptions and errors will
   * continue to bubble out as normal.
   *
   * @param <E> the type of exceptions that can be thrown by the operation
   * @param run the runnable to invoke an wrap checked exceptions from
   * @return a runnable that will wrap all checked exceptions from the provided runnable
   */
  public static <E extends Exception> Runnable wrap(ThrowingRunnable<E> run) {
    return () -> {
      try {
        run.run();
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new WrappedException(e);
      }
    };
  }

  /**
   * Unwraps checked exceptions that would have been wrapped by one of the <code>wrap()</code>>
   * methods invoked by the specified code.
   *
   * <p><i>Note:</i> This version of the <code>unwrap()</code> methods allows a result to be
   * retrieved from the specified code.
   *
   * @param <R> the type of result to be returned from the specified code
   * @param <E> the type of checked exception to be unwrapped
   * @param exceptionClass the class of the exception to be unwrapped
   * @param callable the code to execute and unwrap any wrapped exceptions thrown out from
   * @return the result from the code
   * @throws E the checked exception that is wrapped somewhere in the execution of the specified
   *     callable
   */
  public static <E extends Exception, R> R unwrap(Class<E> exceptionClass, Supplier<R> callable)
      throws E {
    return ExceptionHandler.unwrap(exceptionClass, RuntimeException.class, callable);
  }

  /**
   * Unwraps checked exceptions that would have been wrapped by one of the <code>wrap()</code>>
   * methods invoked by the specified code.
   *
   * <p><i>Note:</i> This version of the <code>unwrap()</code> methods allows a result to be
   * retrieved from the specified callable.
   *
   * @param <E> the type of checked exception to be unwrapped
   * @param exceptionClass the class of the exception to be unwrapped
   * @param run the code to execute and unwrap any wrapped exceptions thrown out from
   * @throws E the checked exception that is wrapped somewhere in the execution of the specified
   *     code
   */
  public static <E extends Exception> void unwrap(Class<E> exceptionClass, Runnable run) throws E {
    ExceptionHandler.unwrap(exceptionClass, RuntimeException.class, run);
  }

  /**
   * Unwraps checked exceptions that would have been wrapped by one of the <code>wrap()</code>>
   * methods invoked by the specified code.
   *
   * <p><i>Note:</i> This version of the <code>unwrap()</code> methods allows a result to be
   * retrieved from the specified code.
   *
   * @param <E> the type of checked exception to be unwrapped
   * @param <F> the type of checked exception to be unwrapped
   * @param <R> the type of result to be returned from the specified code
   * @param exceptionClass the class of the exception to be unwrapped
   * @param exceptionClass2 the class of another exception to be unwrapped
   * @param callable the code to execute and unwrap any wrapped exceptions thrown out from
   * @return the result from the code
   * @throws E the checked exception that is wrapped somewhere in the execution of the specified
   *     callable
   * @throws F the checked exception that is wrapped somewhere in the execution of the specified
   *     callable
   */
  public static <E extends Exception, F extends Exception, R> R unwrap(
      Class<E> exceptionClass, Class<F> exceptionClass2, Supplier<R> callable) throws E, F {
    try {
      return callable.get();
    } catch (WrappedException e) {
      final Throwable t = e.getCause();

      if (exceptionClass.isInstance(t)) {
        throw exceptionClass.cast(t);
      } else if (exceptionClass2.isInstance(t)) {
        throw exceptionClass2.cast(t);
      } else if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      } else if (t instanceof Error) {
        throw (Error) t;
      } // else - just throw our wrapped as a runtime exception since the caller didn't properly
      //          configure the unwrap() in the first place
      throw e;
    }
  }

  /**
   * Unwraps checked exceptions that would have been wrapped by one of the <code>wrap()</code>>
   * methods invoked by the specified code.
   *
   * <p><i>Note:</i> This version of the <code>unwrap()</code> methods allows a result to be
   * retrieved from the specified callable.
   *
   * @param <E> the type of checked exception to be unwrapped
   * @param <F> the type of checked exception to be unwrapped
   * @param exceptionClass the class of the exception to be unwrapped
   * @param exceptionClass2 the class of another exception to be unwrapped
   * @param run the code to execute and unwrap any wrapped exceptions thrown out from
   * @throws E the checked exception that is wrapped somewhere in the execution of the specified
   *     code
   * @throws F the checked exception that is wrapped somewhere in the execution of the specified
   *     code
   */
  public static <E extends Exception, F extends Exception> void unwrap(
      Class<E> exceptionClass, Class<F> exceptionClass2, Runnable run) throws E, F {
    try {
      run.run();
    } catch (WrappedException e) {
      final Throwable t = e.getCause();

      if (exceptionClass.isInstance(t)) {
        throw exceptionClass.cast(t);
      } else if (exceptionClass2.isInstance(t)) {
        throw exceptionClass2.cast(t);
      } else if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      } else if (t instanceof Error) {
        throw (Error) t;
      } // else - just throw our wrapped as a runtime exception since the caller didn't properly
      //          configure the unwrap() in the first place
      throw e;
    }
  }

  /**
   * Internal runtime exception used to translate a checked exception into a runtime one so it can
   * be unwrapped later.
   */
  @VisibleForTesting
  static class WrappedException extends RuntimeException {
    public WrappedException(Throwable cause) {
      super(cause);
    }
  }

  /** Prevents instantiation of this class. */
  private ExceptionHandler() {}
}

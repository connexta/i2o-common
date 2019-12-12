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
package com.connexta.commons.interruptions;

import com.connexta.commons.function.BiThrowingConsumer;
import com.connexta.commons.function.BiThrowingFunction;
import com.connexta.commons.function.BiThrowingRunnable;
import com.connexta.commons.function.BiThrowingSupplier;
import java.io.InterruptedIOException;

/** This class provide useful methods for working with interruptions. */
public class InterruptionHandler {
  /**
   * Performs the specified interruptable code in a loop until such time as it terminates without
   * being interrupted.
   *
   * <p><i>Note:</i> If code will be re-executed from the beginning if it is interrupted and after
   * it terminates successfully or not, the interruption will be propagated back to the current
   * thread.
   *
   * @param <E> the type of exceptions that can be thrown by the specified code
   * @param run the code to run
   * @throws E if the error occurred while invoking the specified code
   */
  @SuppressWarnings({
    "squid:S1181", /* designed to handle throwables by bubbling out virtual machine errors */
    "squid:S1193" /* forced to handle them like this since the code declare exceptions using generics */
  })
  public static <E extends Exception> void runUninterruptedly(
      BiThrowingRunnable<E, InterruptedException> run) throws E {
    boolean interrupted = false;

    try {
      while (true) {
        try {
          run.run();
          return;
        } catch (InterruptedException e) {
          interrupted = true;
        } catch (VirtualMachineError e) {
          throw e;
        } catch (Throwable t) {
          if (InterruptedIOException.class.isInstance(t) || (t instanceof Interruption)) {
            interrupted = true;
            continue;
          }
          throw t;
        }
      }
    } finally {
      if (interrupted) { // propagate the interruption
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Performs the specified interruptable code in a loop until such time as it terminates without
   * being interrupted.
   *
   * <p><i>Note:</i> If code will be re-executed from the beginning if it is interrupted and after
   * it terminates successfully or not, the interruption will be propagated back to the current
   * thread.
   *
   * @param <T> the type of the input to the operation
   * @param <E> the type of exceptions that can be thrown by the specified code
   * @param code the code to call
   * @return a result
   * @throws E if the error occurred while invoking the specified code
   */
  @SuppressWarnings({
    "squid:S1181", /* designed to handle throwables by bubbling out virtual machine errors */
    "squid:S1193" /* forced to handle them like this since the code declare exceptions using generics */
  })
  public static <T, E extends Exception> T getUninterruptedly(
      BiThrowingSupplier<T, E, InterruptedException> code) throws E {
    boolean interrupted = false;

    try {
      while (true) {
        try {
          return code.get();
        } catch (InterruptedException e) {
          interrupted = true;
        } catch (VirtualMachineError e) {
          throw e;
        } catch (Throwable t) {
          if (InterruptedIOException.class.isInstance(t) || (t instanceof Interruption)) {
            interrupted = true;
            continue;
          }
          throw t;
        }
      }
    } finally {
      if (interrupted) { // propagate the interruption
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Performs the specified interruptable code in a loop until such time as it terminates without
   * being interrupted.
   *
   * <p><i>Note:</i> If code will be re-executed from the beginning if it is interrupted and after
   * it terminates successfully or not, the interruption will be propagated back to the current
   * thread.
   *
   * @param <T> the type of the input to the function
   * @param <R> the type of the result of the function
   * @param <E> the type of exceptions that can be thrown by the specified code
   * @param t the function argument
   * @param code the function to call
   * @return the function result
   * @throws E if the error occurred while invoking the specified code
   */
  @SuppressWarnings({
    "squid:S1181", /* designed to handle throwables by bubbling out virtual machine errors */
    "squid:S1193" /* forced to handle them like this since the code declare exceptions using generics */
  })
  public static <T, R, E extends Exception> R applyUninterruptedly(
      T t, BiThrowingFunction<T, R, E, InterruptedException> code) throws E {
    boolean interrupted = false;

    try {
      while (true) {
        try {
          return code.apply(t);
        } catch (InterruptedException e) {
          interrupted = true;
        } catch (VirtualMachineError e) {
          throw e;
        } catch (Throwable e) {
          if (InterruptedIOException.class.isInstance(e) || (e instanceof Interruption)) {
            interrupted = true;
            continue;
          }
          throw e;
        }
      }
    } finally {
      if (interrupted) { // propagate the interruption
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Performs the specified interruptable code in a loop until such time as it terminates without
   * being interrupted.
   *
   * <p><i>Note:</i> If code will be re-executed from the beginning if it is interrupted and after
   * it terminates successfully or not, the interruption will be propagated back to the current
   * thread.
   *
   * @param <T> the type of the input to the operation
   * @param <E> the type of exceptions that can be thrown by the specified code
   * @param t the input argument
   * @param code the code to call
   * @throws E if the error occurred while invoking the specified code
   */
  @SuppressWarnings({
    "squid:S1181", /* designed to handle throwables by bubbling out virtual machine errors */
    "squid:S1193" /* forced to handle them like this since the code declare exceptions using generics */
  })
  public static <T, E extends Exception> void acceptUninterruptedly(
      T t, BiThrowingConsumer<T, E, InterruptedException> code) throws E {
    boolean interrupted = false;

    try {
      while (true) {
        try {
          code.accept(t);
          return;
        } catch (InterruptedException e) {
          interrupted = true;
        } catch (VirtualMachineError e) {
          throw e;
        } catch (Throwable e) {
          if (InterruptedIOException.class.isInstance(e) || (e instanceof Interruption)) {
            interrupted = true;
            continue;
          }
          throw e;
        }
      }
    } finally {
      if (interrupted) { // propagate the interruption
        Thread.currentThread().interrupt();
      }
    }
  }

  /** Prevents instantiation of this class. */
  private InterruptionHandler() {}
}

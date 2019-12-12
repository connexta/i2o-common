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

import com.connexta.commons.exceptions.ExceptionHandler.WrappedException;
import com.connexta.commons.function.ThrowingConsumer;
import com.connexta.commons.function.ThrowingFunction;
import com.connexta.commons.function.ThrowingRunnable;
import com.connexta.commons.function.ThrowingSupplier;
import java.io.IOException;
import java.util.function.Supplier;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class ExceptionHandlerTest {
  private static final Object ARG = "argument";
  private static final Object RESULT = "result";
  private static final Error ERROR = new Error("testing");
  private static final RuntimeException RUNTIME_EXCEPTION = new RuntimeException("testing");
  private static final InterruptedException EXCEPTION = new InterruptedException("testing");
  private static final IOException EXCEPTION2 = new IOException("testing");
  private static final Class<InterruptedException> EXCEPTION_CLASS = InterruptedException.class;
  private static final Class<IOException> EXCEPTION_CLASS2 = IOException.class;
  private static final Class<ClassNotFoundException> EXCEPTION_CLASS3 =
      ClassNotFoundException.class;

  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testWrapFunction() throws Exception {
    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenReturn(ExceptionHandlerTest.RESULT);

    Assert.assertThat(
        ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG),
        Matchers.sameInstance(ExceptionHandlerTest.RESULT));

    Mockito.verify(lambda).apply(Mockito.same(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testWrapFunctionBubblesOutError() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.ERROR));

    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenThrow(ExceptionHandlerTest.ERROR);

    ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG);

    Mockito.verify(lambda).apply(Mockito.same(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testWrapFunctionBubblesOutRuntimeException() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.RUNTIME_EXCEPTION));

    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenThrow(ExceptionHandlerTest.RUNTIME_EXCEPTION);

    ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG);

    Mockito.verify(lambda).apply(Mockito.same(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testWrapFunctionWrapsCheckedException() throws Exception {
    exception.expect(Matchers.isA(WrappedException.class));
    exception.expectCause(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION));

    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenThrow(ExceptionHandlerTest.EXCEPTION);

    ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG);

    Mockito.verify(lambda).apply(Mockito.same(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testWrapSupplier() throws Exception {
    final ThrowingSupplier<Object, Exception> lambda = Mockito.mock(ThrowingSupplier.class);

    Mockito.when(lambda.get()).thenReturn(ExceptionHandlerTest.RESULT);

    Assert.assertThat(
        ExceptionHandler.wrap(lambda).get(), Matchers.sameInstance(ExceptionHandlerTest.RESULT));

    Mockito.verify(lambda).get();
  }

  @Test
  public void testWrapSupplierBubblesOutError() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.ERROR));

    final ThrowingSupplier<Object, Exception> lambda = Mockito.mock(ThrowingSupplier.class);

    Mockito.when(lambda.get()).thenThrow(ExceptionHandlerTest.ERROR);

    ExceptionHandler.wrap(lambda).get();

    Mockito.verify(lambda).get();
  }

  @Test
  public void testWrapSupplierBubblesOutRuntimeException() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.RUNTIME_EXCEPTION));

    final ThrowingSupplier<Object, Exception> lambda = Mockito.mock(ThrowingSupplier.class);

    Mockito.when(lambda.get()).thenThrow(ExceptionHandlerTest.RUNTIME_EXCEPTION);

    ExceptionHandler.wrap(lambda).get();

    Mockito.verify(lambda).get();
  }

  @Test
  public void testWrapSupplierWrapsCheckedException() throws Exception {
    exception.expect(Matchers.isA(WrappedException.class));
    exception.expectCause(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION));

    final ThrowingSupplier<Object, Exception> lambda = Mockito.mock(ThrowingSupplier.class);

    Mockito.when(lambda.get()).thenThrow(ExceptionHandlerTest.EXCEPTION);

    ExceptionHandler.wrap(lambda).get();

    Mockito.verify(lambda).get();
  }

  @Test
  public void testWrapConsumer() throws Exception {
    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG);

    Mockito.verify(lambda).accept(Mockito.same(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testWrapConsumerBubblesOutError() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.ERROR));

    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    Mockito.doThrow(ExceptionHandlerTest.ERROR).when(lambda).accept(Mockito.any());

    ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG);

    Mockito.verify(lambda).accept(Mockito.same(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testWrapConsumerBubblesOutRuntimeException() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.RUNTIME_EXCEPTION));

    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    Mockito.doThrow(ExceptionHandlerTest.RUNTIME_EXCEPTION).when(lambda).accept(Mockito.any());

    ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG);

    Mockito.verify(lambda).accept(Mockito.same(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testWrapConsumerWrapsCheckedException() throws Exception {
    exception.expect(Matchers.isA(WrappedException.class));
    exception.expectCause(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION));

    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    Mockito.doThrow(ExceptionHandlerTest.EXCEPTION).when(lambda).accept(Mockito.any());

    ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG);

    Mockito.verify(lambda).accept(Mockito.same(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testWrapRunnable() throws Exception {
    final ThrowingRunnable<Exception> lambda = Mockito.mock(ThrowingRunnable.class);

    ExceptionHandler.wrap(lambda).run();

    Mockito.verify(lambda).run();
  }

  @Test
  public void testWrapRunnableBubblesOutError() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.ERROR));

    final ThrowingRunnable<Exception> lambda = Mockito.mock(ThrowingRunnable.class);

    Mockito.doThrow(ExceptionHandlerTest.ERROR).when(lambda).run();

    ExceptionHandler.wrap(lambda).run();

    Mockito.verify(lambda).run();
  }

  @Test
  public void testWrapRunnableBubblesOutRuntimeException() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.RUNTIME_EXCEPTION));

    final ThrowingRunnable<Exception> lambda = Mockito.mock(ThrowingRunnable.class);

    Mockito.doThrow(ExceptionHandlerTest.RUNTIME_EXCEPTION).when(lambda).run();

    ExceptionHandler.wrap(lambda).run();

    Mockito.verify(lambda).run();
  }

  @Test
  public void testWrapRunnableWrapsCheckedException() throws Exception {
    exception.expect(Matchers.isA(WrappedException.class));
    exception.expectCause(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION));

    final ThrowingRunnable<Exception> lambda = Mockito.mock(ThrowingRunnable.class);

    Mockito.doThrow(ExceptionHandlerTest.EXCEPTION).when(lambda).run();

    ExceptionHandler.wrap(lambda).run();

    Mockito.verify(lambda).run();
  }

  @Test
  public void testUnwrapSupplierWithOneException() throws Exception {
    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenReturn(ExceptionHandlerTest.RESULT);

    Assert.assertThat(
        ExceptionHandler.unwrap(
            ExceptionHandlerTest.EXCEPTION_CLASS,
            () -> ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG)),
        Matchers.sameInstance(ExceptionHandlerTest.RESULT));
  }

  @Test
  public void testUnwrapSupplierWithOneExceptionBubblesOutError() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.ERROR));

    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenThrow(ExceptionHandlerTest.ERROR);

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        () -> ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapSupplierWithOneExceptionBubblesOutRuntimeException() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.RUNTIME_EXCEPTION));

    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenThrow(ExceptionHandlerTest.RUNTIME_EXCEPTION);

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        () -> ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapSupplierWithOneExceptionThrowsCheckedException() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION));

    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenThrow(ExceptionHandlerTest.EXCEPTION);

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        () -> ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapSupplierWithOneExceptionIsUnableToUnwrap() throws Exception {
    exception.expect(Matchers.isA(WrappedException.class));
    exception.expectCause(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION));

    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenThrow(ExceptionHandlerTest.EXCEPTION);

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS3,
        () -> ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapSupplierWithTwoExceptions() throws Exception {
    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenReturn(ExceptionHandlerTest.RESULT);

    Assert.assertThat(
        ExceptionHandler.unwrap(
            ExceptionHandlerTest.EXCEPTION_CLASS,
            ExceptionHandlerTest.EXCEPTION_CLASS2,
            () -> ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG)),
        Matchers.sameInstance(ExceptionHandlerTest.RESULT));
  }

  @Test
  public void testUnwrapSupplierWithTwoExceptionsBubblesOutError() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.ERROR));

    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenThrow(ExceptionHandlerTest.ERROR);

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        () -> {
          throw new WrappedException(ExceptionHandlerTest.ERROR);
        });
  }

  @Test
  public void testUnwrapSupplierWithTwoExceptionsBubblesOutError2() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.ERROR));

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        (Supplier)
            () -> {
              throw new WrappedException(ExceptionHandlerTest.ERROR);
            });
  }

  @Test
  public void testUnwrapSupplierWithTwoExceptionsBubblesOutRuntimeException() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.RUNTIME_EXCEPTION));

    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenThrow(ExceptionHandlerTest.RUNTIME_EXCEPTION);

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        () -> ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapSupplierWithTwoExceptionsBubblesOutRuntimeException2() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.RUNTIME_EXCEPTION));

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        (Supplier)
            () -> {
              throw new WrappedException(ExceptionHandlerTest.RUNTIME_EXCEPTION);
            });
  }

  @Test
  public void testUnwrapSupplierWithTwoExceptionsThrowsCheckedException() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION));

    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenThrow(ExceptionHandlerTest.EXCEPTION);

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        () -> ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapSupplierWithTwoExceptionsThrowsCheckedException2() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION2));

    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenThrow(ExceptionHandlerTest.EXCEPTION2);

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        () -> ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapSupplierWithTwoExceptionsIfUnableToUnwrap() throws Exception {
    exception.expect(Matchers.isA(WrappedException.class));
    exception.expectCause(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION));

    final ThrowingFunction<Object, Object, Exception> lambda = Mockito.mock(ThrowingFunction.class);

    Mockito.when(lambda.apply(Mockito.any())).thenThrow(ExceptionHandlerTest.EXCEPTION);

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        ExceptionHandlerTest.EXCEPTION_CLASS3,
        () -> ExceptionHandler.wrap(lambda).apply(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapRunnableWithOneException() throws Exception {
    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        () -> ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG));

    Mockito.verify(lambda).accept(Mockito.any());
  }

  @Test
  public void testUnwrapRunnableWithOneExceptionBubblesOutError() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.ERROR));

    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    Mockito.doThrow(ExceptionHandlerTest.ERROR).when(lambda).accept(Mockito.any());

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        () -> ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapRunnableWithOneExceptionBubblesOutRuntimeException() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.RUNTIME_EXCEPTION));

    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    Mockito.doThrow(ExceptionHandlerTest.RUNTIME_EXCEPTION).when(lambda).accept(Mockito.any());

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        () -> ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapRunnableWithOneExceptionThrowsCheckedException() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION));

    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    Mockito.doThrow(ExceptionHandlerTest.EXCEPTION).when(lambda).accept(Mockito.any());

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        () -> ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapRunnableWithTwoExceptions() throws Exception {
    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        () -> ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG));

    Mockito.verify(lambda).accept(Mockito.any());
  }

  @Test
  public void testUnwrapRunnableWithTwoExceptionsBubblesOutError() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.ERROR));

    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    Mockito.doThrow(ExceptionHandlerTest.ERROR).when(lambda).accept(Mockito.any());

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        () -> ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapRunnableWithTwoExceptionsBubblesOutError2() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.ERROR));

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        (Runnable)
            () -> {
              throw new WrappedException(ExceptionHandlerTest.ERROR);
            });
  }

  @Test
  public void testUnwrapRunnableWithTwoExceptionsBubblesOutRuntimeException() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.RUNTIME_EXCEPTION));

    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    Mockito.doThrow(ExceptionHandlerTest.RUNTIME_EXCEPTION).when(lambda).accept(Mockito.any());

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        () -> ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapRunnableWithTwoExceptionsBubblesOutRuntimeException2() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.RUNTIME_EXCEPTION));

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        (Runnable)
            () -> {
              throw new WrappedException(ExceptionHandlerTest.RUNTIME_EXCEPTION);
            });
  }

  @Test
  public void testUnwrapRunnableWithTwoExceptionsThrowsCheckedException() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION));

    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    Mockito.doThrow(ExceptionHandlerTest.EXCEPTION).when(lambda).accept(Mockito.any());

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        () -> ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapRunnableWithTwoExceptionsThrowsCheckedException2() throws Exception {
    exception.expect(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION2));

    final ThrowingConsumer<Object, Exception> lambda = Mockito.mock(ThrowingConsumer.class);

    Mockito.doThrow(ExceptionHandlerTest.EXCEPTION2).when(lambda).accept(Mockito.any());

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS,
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        () -> ExceptionHandler.wrap(lambda).accept(ExceptionHandlerTest.ARG));
  }

  @Test
  public void testUnwrapFunctionWithTwoExceptionsIfUnableToUnwrap() throws Exception {
    exception.expect(Matchers.isA(WrappedException.class));
    exception.expectCause(Matchers.sameInstance(ExceptionHandlerTest.EXCEPTION));

    final ThrowingRunnable<Exception> lambda = Mockito.mock(ThrowingRunnable.class);

    Mockito.doThrow(ExceptionHandlerTest.EXCEPTION).when(lambda).run();

    ExceptionHandler.unwrap(
        ExceptionHandlerTest.EXCEPTION_CLASS2,
        ExceptionHandlerTest.EXCEPTION_CLASS3,
        () -> ExceptionHandler.wrap(lambda).run());
  }
}

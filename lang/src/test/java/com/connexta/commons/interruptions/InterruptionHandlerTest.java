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
import org.codice.junit.ClearInterruptions;
import org.codice.junit.rules.MethodRuleAnnotationProcessor;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

@ClearInterruptions
public class InterruptionHandlerTest {
  private static final InterruptedException INTERRUPTED_EXCEPTION =
      new InterruptedException("testing");
  private static final MyInterruptedException MY_INTERRUPTED = new MyInterruptedException();
  private static final InterruptedIOException INTERRUPTED_IO_EXCEPTION =
      new InterruptedIOException("testing");
  private static final VirtualMachineError VM_ERROR = new StackOverflowError("testing");
  private static final Error ERROR = new Error("testing");
  private static final RuntimeException RUNTIME_EXCEPTION = new RuntimeException("testing");
  private static final Exception EXCEPTION = new Exception("testing");
  private static final String ARG = "arg";
  private static final String RESULT = "result";

  @Rule
  public MethodRuleAnnotationProcessor annotationProcessor = new MethodRuleAnnotationProcessor();

  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testRunUninterruptedly() throws Exception {
    final BiThrowingRunnable<Exception, InterruptedException> run =
        Mockito.mock(BiThrowingRunnable.class);

    Mockito.doNothing().when(run).run();

    InterruptionHandler.runUninterruptedly(run);

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));

    Mockito.verify(run).run();
  }

  @Test
  public void testRunUninterruptedlyWhenInterrupted() throws Exception {
    final BiThrowingRunnable<Exception, InterruptedException> run =
        Mockito.mock(BiThrowingRunnable.class);

    Mockito.doThrow(InterruptionHandlerTest.INTERRUPTED_EXCEPTION).doNothing().when(run).run();

    InterruptionHandler.runUninterruptedly(run);

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(true));

    Mockito.verify(run, Mockito.times(2)).run();
  }

  @Test
  public void testRunUninterruptedlyWhenIOInterrupted() throws Exception {
    final BiThrowingRunnable<Exception, InterruptedException> run =
        Mockito.mock(BiThrowingRunnable.class);

    Mockito.doThrow(InterruptionHandlerTest.INTERRUPTED_IO_EXCEPTION).doNothing().when(run).run();

    InterruptionHandler.runUninterruptedly(run);

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(true));

    Mockito.verify(run, Mockito.times(2)).run();
  }

  @Test
  public void testRunUninterruptedlyWhenInterruptedWithACustomInterruption() throws Exception {
    final BiThrowingRunnable<Exception, InterruptedException> run =
        Mockito.mock(BiThrowingRunnable.class);

    Mockito.doThrow(InterruptionHandlerTest.MY_INTERRUPTED).doNothing().when(run).run();

    InterruptionHandler.runUninterruptedly(run);

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(true));

    Mockito.verify(run, Mockito.times(2)).run();
  }

  @Test
  public void testRunUninterruptedlyBubblesOutCheckedException() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.EXCEPTION));

    final BiThrowingRunnable<Exception, InterruptedException> run =
        Mockito.mock(BiThrowingRunnable.class);

    Mockito.doThrow(InterruptionHandlerTest.EXCEPTION).when(run).run();

    try {
      InterruptionHandler.runUninterruptedly(run);
    } catch (Exception e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(run).run();
      throw e;
    }
  }

  @Test
  public void testRunUninterruptedlyBubblesOutRuntimeException() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.RUNTIME_EXCEPTION));

    final BiThrowingRunnable<Exception, InterruptedException> run =
        Mockito.mock(BiThrowingRunnable.class);

    Mockito.doThrow(InterruptionHandlerTest.RUNTIME_EXCEPTION).when(run).run();

    try {
      InterruptionHandler.runUninterruptedly(run);
    } catch (Error e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(run).run();
      throw e;
    }
  }

  @Test
  public void testRunUninterruptedlyBubblesOutError() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.ERROR));

    final BiThrowingRunnable<Exception, InterruptedException> run =
        Mockito.mock(BiThrowingRunnable.class);

    Mockito.doThrow(InterruptionHandlerTest.ERROR).when(run).run();

    try {
      InterruptionHandler.runUninterruptedly(run);
    } catch (Error e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(run).run();
      throw e;
    }
  }

  @Test
  public void testRunUninterruptedlyBubblesOutVirtualMachineError() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.VM_ERROR));

    final BiThrowingRunnable<Exception, InterruptedException> run =
        Mockito.mock(BiThrowingRunnable.class);

    Mockito.doThrow(InterruptionHandlerTest.VM_ERROR).when(run).run();

    try {
      InterruptionHandler.runUninterruptedly(run);
    } catch (Exception e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(run).run();
      throw e;
    }
  }

  @Test
  public void testGetUninterruptedly() throws Exception {
    final BiThrowingSupplier<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingSupplier.class);

    Mockito.doReturn(InterruptionHandlerTest.RESULT).when(code).get();

    Assert.assertThat(
        InterruptionHandler.getUninterruptedly(code),
        Matchers.sameInstance(InterruptionHandlerTest.RESULT));

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));

    Mockito.verify(code).get();
  }

  @Test
  public void testGetUninterruptedlyWhenInterrupted() throws Exception {
    final BiThrowingSupplier<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingSupplier.class);

    Mockito.doThrow(InterruptionHandlerTest.INTERRUPTED_EXCEPTION)
        .doReturn(InterruptionHandlerTest.RESULT)
        .when(code)
        .get();

    Assert.assertThat(
        InterruptionHandler.getUninterruptedly(code),
        Matchers.sameInstance(InterruptionHandlerTest.RESULT));

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(true));

    Mockito.verify(code, Mockito.times(2)).get();
  }

  @Test
  public void testGetUninterruptedlyWhenIOInterrupted() throws Exception {
    final BiThrowingSupplier<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingSupplier.class);

    Mockito.doThrow(InterruptionHandlerTest.INTERRUPTED_IO_EXCEPTION)
        .doReturn(InterruptionHandlerTest.RESULT)
        .when(code)
        .get();

    Assert.assertThat(
        InterruptionHandler.getUninterruptedly(code),
        Matchers.sameInstance(InterruptionHandlerTest.RESULT));

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(true));

    Mockito.verify(code, Mockito.times(2)).get();
  }

  @Test
  public void testGetUninterruptedlyWhenInterruptedWithACustomInterruption() throws Exception {
    final BiThrowingSupplier<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingSupplier.class);

    Mockito.doThrow(InterruptionHandlerTest.MY_INTERRUPTED)
        .doReturn(InterruptionHandlerTest.RESULT)
        .when(code)
        .get();

    Assert.assertThat(
        InterruptionHandler.getUninterruptedly(code),
        Matchers.sameInstance(InterruptionHandlerTest.RESULT));

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(true));

    Mockito.verify(code, Mockito.times(2)).get();
  }

  @Test
  public void testGetUninterruptedlyBubblesOutCheckedException() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.EXCEPTION));

    final BiThrowingSupplier<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingSupplier.class);

    Mockito.doThrow(InterruptionHandlerTest.EXCEPTION).when(code).get();

    try {
      InterruptionHandler.getUninterruptedly(code);
    } catch (Exception e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(code).get();
      throw e;
    }
  }

  @Test
  public void testGetUninterruptedlyBubblesOutRuntimeException() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.RUNTIME_EXCEPTION));

    final BiThrowingSupplier<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingSupplier.class);

    Mockito.doThrow(InterruptionHandlerTest.RUNTIME_EXCEPTION).when(code).get();

    try {
      InterruptionHandler.getUninterruptedly(code);
    } catch (Exception e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(code).get();
      throw e;
    }
  }

  @Test
  public void testGetUninterruptedlyBubblesOutError() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.ERROR));

    final BiThrowingSupplier<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingSupplier.class);

    Mockito.doThrow(InterruptionHandlerTest.ERROR).when(code).get();

    try {
      InterruptionHandler.getUninterruptedly(code);
    } catch (Error e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(code).get();
      throw e;
    }
  }

  @Test
  public void testGetUninterruptedlyBubblesOutVirtualMachineError() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.VM_ERROR));

    final BiThrowingSupplier<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingSupplier.class);

    Mockito.doThrow(InterruptionHandlerTest.VM_ERROR).when(code).get();

    try {
      InterruptionHandler.getUninterruptedly(code);
    } catch (Error e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(code).get();
      throw e;
    }
  }

  @Test
  public void testApplyUninterruptedly() throws Exception {
    final BiThrowingFunction<String, String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingFunction.class);

    Mockito.doReturn(InterruptionHandlerTest.RESULT).when(code).apply(InterruptionHandlerTest.ARG);

    Assert.assertThat(
        InterruptionHandler.applyUninterruptedly(InterruptionHandlerTest.ARG, code),
        Matchers.sameInstance(InterruptionHandlerTest.RESULT));

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));

    Mockito.verify(code).apply(InterruptionHandlerTest.ARG);
  }

  @Test
  public void testApplyUninterruptedlyWhenInterrupted() throws Exception {
    final BiThrowingFunction<String, String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingFunction.class);

    Mockito.doThrow(InterruptionHandlerTest.INTERRUPTED_EXCEPTION)
        .doReturn(InterruptionHandlerTest.RESULT)
        .when(code)
        .apply(InterruptionHandlerTest.ARG);

    Assert.assertThat(
        InterruptionHandler.applyUninterruptedly(InterruptionHandlerTest.ARG, code),
        Matchers.sameInstance(InterruptionHandlerTest.RESULT));

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(true));

    Mockito.verify(code, Mockito.times(2)).apply(InterruptionHandlerTest.ARG);
  }

  @Test
  public void testApplyUninterruptedlyWhenIOInterrupted() throws Exception {
    final BiThrowingFunction<String, String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingFunction.class);

    Mockito.doThrow(InterruptionHandlerTest.INTERRUPTED_IO_EXCEPTION)
        .doReturn(InterruptionHandlerTest.RESULT)
        .when(code)
        .apply(InterruptionHandlerTest.ARG);

    Assert.assertThat(
        InterruptionHandler.applyUninterruptedly(InterruptionHandlerTest.ARG, code),
        Matchers.sameInstance(InterruptionHandlerTest.RESULT));

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(true));

    Mockito.verify(code, Mockito.times(2)).apply(InterruptionHandlerTest.ARG);
  }

  @Test
  public void testApplyUninterruptedlyWhenInterruptedWithACustomInterruption() throws Exception {
    final BiThrowingFunction<String, String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingFunction.class);

    Mockito.doThrow(InterruptionHandlerTest.MY_INTERRUPTED)
        .doReturn(InterruptionHandlerTest.RESULT)
        .when(code)
        .apply(InterruptionHandlerTest.ARG);

    Assert.assertThat(
        InterruptionHandler.applyUninterruptedly(InterruptionHandlerTest.ARG, code),
        Matchers.sameInstance(InterruptionHandlerTest.RESULT));

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(true));

    Mockito.verify(code, Mockito.times(2)).apply(InterruptionHandlerTest.ARG);
  }

  @Test
  public void testApplyUninterruptedlyBubblesOutCheckedException() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.EXCEPTION));

    final BiThrowingFunction<String, String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingFunction.class);

    Mockito.doThrow(InterruptionHandlerTest.EXCEPTION)
        .when(code)
        .apply(InterruptionHandlerTest.ARG);

    try {
      InterruptionHandler.applyUninterruptedly(InterruptionHandlerTest.ARG, code);
    } catch (Exception e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(code).apply(InterruptionHandlerTest.ARG);
      throw e;
    }
  }

  @Test
  public void testApplyUninterruptedlyBubblesOutRuntimeException() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.RUNTIME_EXCEPTION));

    final BiThrowingFunction<String, String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingFunction.class);

    Mockito.doThrow(InterruptionHandlerTest.RUNTIME_EXCEPTION)
        .when(code)
        .apply(InterruptionHandlerTest.ARG);

    try {
      InterruptionHandler.applyUninterruptedly(InterruptionHandlerTest.ARG, code);
    } catch (Exception e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(code).apply(InterruptionHandlerTest.ARG);
      throw e;
    }
  }

  @Test
  public void testApplyUninterruptedlyBubblesOutError() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.ERROR));

    final BiThrowingFunction<String, String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingFunction.class);

    Mockito.doThrow(InterruptionHandlerTest.ERROR).when(code).apply(InterruptionHandlerTest.ARG);

    try {
      InterruptionHandler.applyUninterruptedly(InterruptionHandlerTest.ARG, code);
    } catch (Error e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(code).apply(InterruptionHandlerTest.ARG);
      throw e;
    }
  }

  @Test
  public void testApplyUninterruptedlyBubblesOutVirtualMachineError() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.VM_ERROR));

    final BiThrowingFunction<String, String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingFunction.class);

    Mockito.doThrow(InterruptionHandlerTest.VM_ERROR).when(code).apply(InterruptionHandlerTest.ARG);

    try {
      InterruptionHandler.applyUninterruptedly(InterruptionHandlerTest.ARG, code);
    } catch (Error e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(code).apply(InterruptionHandlerTest.ARG);
      throw e;
    }
  }

  @Test
  public void testAcceptUninterruptedly() throws Exception {
    final BiThrowingConsumer<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingConsumer.class);

    Mockito.doNothing().when(code).accept(InterruptionHandlerTest.ARG);

    InterruptionHandler.acceptUninterruptedly(InterruptionHandlerTest.ARG, code);

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));

    Mockito.verify(code).accept(InterruptionHandlerTest.ARG);
  }

  @Test
  public void testAcceptUninterruptedlyWhenInterrupted() throws Exception {
    final BiThrowingConsumer<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingConsumer.class);

    Mockito.doThrow(InterruptionHandlerTest.INTERRUPTED_EXCEPTION)
        .doNothing()
        .when(code)
        .accept(InterruptionHandlerTest.ARG);

    InterruptionHandler.acceptUninterruptedly(InterruptionHandlerTest.ARG, code);

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(true));

    Mockito.verify(code, Mockito.times(2)).accept(InterruptionHandlerTest.ARG);
  }

  @Test
  public void testAcceptUninterruptedlyWhenIOInterrupted() throws Exception {
    final BiThrowingConsumer<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingConsumer.class);

    Mockito.doThrow(InterruptionHandlerTest.INTERRUPTED_IO_EXCEPTION)
        .doNothing()
        .when(code)
        .accept(InterruptionHandlerTest.ARG);

    InterruptionHandler.acceptUninterruptedly(InterruptionHandlerTest.ARG, code);

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(true));

    Mockito.verify(code, Mockito.times(2)).accept(InterruptionHandlerTest.ARG);
  }

  @Test
  public void testAcceptUninterruptedlyWhenInterruptedWithACustomInterruption() throws Exception {
    final BiThrowingConsumer<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingConsumer.class);

    Mockito.doThrow(InterruptionHandlerTest.MY_INTERRUPTED)
        .doNothing()
        .when(code)
        .accept(InterruptionHandlerTest.ARG);

    InterruptionHandler.acceptUninterruptedly(InterruptionHandlerTest.ARG, code);

    Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(true));

    Mockito.verify(code, Mockito.times(2)).accept(InterruptionHandlerTest.ARG);
  }

  @Test
  public void testAcceptUninterruptedlyBubblesOutCheckedException() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.EXCEPTION));

    final BiThrowingConsumer<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingConsumer.class);

    Mockito.doThrow(InterruptionHandlerTest.EXCEPTION)
        .when(code)
        .accept(InterruptionHandlerTest.ARG);

    try {
      InterruptionHandler.acceptUninterruptedly(InterruptionHandlerTest.ARG, code);
    } catch (Exception e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(code).accept(InterruptionHandlerTest.ARG);
      throw e;
    }
  }

  @Test
  public void testAcceptUninterruptedlyBubblesOutRuntimeException() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.RUNTIME_EXCEPTION));

    final BiThrowingConsumer<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingConsumer.class);

    Mockito.doThrow(InterruptionHandlerTest.RUNTIME_EXCEPTION)
        .when(code)
        .accept(InterruptionHandlerTest.ARG);

    try {
      InterruptionHandler.acceptUninterruptedly(InterruptionHandlerTest.ARG, code);
    } catch (Exception e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(code).accept(InterruptionHandlerTest.ARG);
      throw e;
    }
  }

  @Test
  public void testAcceptUninterruptedlyBubblesOutError() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.ERROR));

    final BiThrowingConsumer<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingConsumer.class);

    Mockito.doThrow(InterruptionHandlerTest.ERROR).when(code).accept(InterruptionHandlerTest.ARG);

    try {
      InterruptionHandler.acceptUninterruptedly(InterruptionHandlerTest.ARG, code);
    } catch (Error e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(code).accept(InterruptionHandlerTest.ARG);
      throw e;
    }
  }

  @Test
  public void testAcceptUninterruptedlyBubblesOutVirtualMachineError() throws Exception {
    exception.expect(Matchers.sameInstance(InterruptionHandlerTest.VM_ERROR));

    final BiThrowingConsumer<String, Exception, InterruptedException> code =
        Mockito.mock(BiThrowingConsumer.class);

    Mockito.doThrow(InterruptionHandlerTest.VM_ERROR)
        .when(code)
        .accept(InterruptionHandlerTest.ARG);

    try {
      InterruptionHandler.acceptUninterruptedly(InterruptionHandlerTest.ARG, code);
    } catch (Error e) {
      Assert.assertThat(Thread.currentThread().isInterrupted(), Matchers.equalTo(false));
      Mockito.verify(code).accept(InterruptionHandlerTest.ARG);
      throw e;
    }
  }

  private static class MyInterruptedException extends Exception implements Interruption {}
}

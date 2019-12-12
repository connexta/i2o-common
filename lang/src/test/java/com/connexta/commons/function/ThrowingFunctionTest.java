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
package com.connexta.commons.function;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class ThrowingFunctionTest {
  private static final Object DATA = new Object();

  @Rule public ExpectedException exception = ExpectedException.none();

  private final ThrowingFunction function =
      Mockito.mock(ThrowingFunction.class, Mockito.CALLS_REAL_METHODS);

  @Test
  public void testIdentity() throws Exception {
    Assert.assertThat(
        ThrowingFunction.identity().apply(ThrowingFunctionTest.DATA),
        Matchers.sameInstance(ThrowingFunctionTest.DATA));
  }

  @Test
  public void testLimitedWhenThrowingASubclass() throws Exception {
    final BaseException e = new SubException();

    exception.expect(Matchers.sameInstance(e));

    final ThrowingFunction<String, String, BaseException> f =
        a -> {
          throw e;
        };

    f.limitedTo(BaseException.class, BaseException::new).apply("dummy");
  }

  @Test
  public void testLimitedWhenThrowingTheClass() throws Exception {
    final BaseException e = new BaseException();

    exception.expect(Matchers.sameInstance(e));

    final ThrowingFunction<String, String, BaseException> f =
        a -> {
          throw e;
        };

    f.limitedTo(BaseException.class, BaseException::new).apply("dummy");
  }

  @Test
  public void testLimitedWhenThrowingARuntimeException() throws Exception {
    final RuntimeException e = new RuntimeException();

    exception.expect(Matchers.sameInstance(e));

    final ThrowingFunction<String, String, BaseException> f =
        a -> {
          throw e;
        };

    f.limitedTo(BaseException.class, BaseException::new).apply("dummy");
  }

  @Test
  public void testLimitedWhenThrowingSomethingElse() throws Exception {
    final Exception e = new Exception();

    exception.expect(Matchers.isA(BaseException.class));
    exception.expectCause(Matchers.sameInstance(e));

    final ThrowingFunction<String, String, Exception> f =
        a -> {
          throw e;
        };

    f.limitedTo(BaseException.class, BaseException::new).apply("dummy");
  }

  private static class BaseException extends Exception {
    public BaseException() {
      super();
    }

    public BaseException(Throwable cause) {
      super(cause);
    }
  }

  private static class SubException extends BaseException {
    public SubException() {
      super();
    }

    public SubException(Throwable cause) {
      super(cause);
    }
  }
}

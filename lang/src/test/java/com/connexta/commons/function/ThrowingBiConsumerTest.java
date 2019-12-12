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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class ThrowingBiConsumerTest {
  private static final String DATA = "data";
  private static final Integer DATA2 = 123;
  private static final Exception ERROR = new Exception("testing");

  @Rule public ExpectedException exception = ExpectedException.none();

  private final ThrowingBiConsumer before =
      Mockito.mock(ThrowingBiConsumer.class, Mockito.CALLS_REAL_METHODS);
  private final ThrowingBiConsumer after =
      Mockito.mock(ThrowingBiConsumer.class, Mockito.CALLS_REAL_METHODS);

  @Test
  public void testAndThen() throws Exception {
    Mockito.doNothing().when(before).accept(Mockito.any(), Mockito.any());
    Mockito.doNothing().when(after).accept(Mockito.any(), Mockito.any());

    before.andThen(after).accept(ThrowingBiConsumerTest.DATA, ThrowingBiConsumerTest.DATA2);

    final InOrder inOrder = Mockito.inOrder(before, after);

    inOrder.verify(before).accept(ThrowingBiConsumerTest.DATA, ThrowingBiConsumerTest.DATA2);
    inOrder.verify(after).accept(ThrowingBiConsumerTest.DATA, ThrowingBiConsumerTest.DATA2);
  }

  @Test
  public void testAndThenThrowsExceptionFromFirstWithoutCallingSecond() throws Exception {
    exception.expect(Matchers.sameInstance(ThrowingBiConsumerTest.ERROR));

    Mockito.doThrow(ThrowingBiConsumerTest.ERROR).when(before).accept(Mockito.any(), Mockito.any());

    try {
      before.andThen(after).accept(ThrowingBiConsumerTest.DATA, ThrowingBiConsumerTest.DATA2);
    } catch (Exception e) {
      Mockito.verify(before).accept(ThrowingBiConsumerTest.DATA, ThrowingBiConsumerTest.DATA2);
      Mockito.verify(after, Mockito.never()).accept(Mockito.any(), Mockito.any());
      throw e;
    }
  }

  @Test
  public void testAndThenThrowsExceptionFromSecondAfterCallToFirst() throws Exception {
    exception.expect(Matchers.sameInstance(ThrowingBiConsumerTest.ERROR));

    Mockito.doNothing().when(before).accept(Mockito.any(), Mockito.any());
    Mockito.doThrow(ThrowingBiConsumerTest.ERROR).when(after).accept(Mockito.any(), Mockito.any());

    try {
      before.andThen(after).accept(ThrowingBiConsumerTest.DATA, ThrowingBiConsumerTest.DATA2);
    } catch (Exception e) {
      final InOrder inOrder = Mockito.inOrder(before, after);

      inOrder.verify(before).accept(ThrowingBiConsumerTest.DATA, ThrowingBiConsumerTest.DATA2);
      inOrder.verify(after).accept(ThrowingBiConsumerTest.DATA, ThrowingBiConsumerTest.DATA2);
      throw e;
    }
  }
}

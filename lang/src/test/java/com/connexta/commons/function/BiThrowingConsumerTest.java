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

public class BiThrowingConsumerTest {
  private static final String DATA = "data";
  private static final Exception ERROR = new Exception("testing");

  @Rule public ExpectedException exception = ExpectedException.none();

  private final BiThrowingConsumer before =
      Mockito.mock(BiThrowingConsumer.class, Mockito.CALLS_REAL_METHODS);
  private final BiThrowingConsumer after =
      Mockito.mock(BiThrowingConsumer.class, Mockito.CALLS_REAL_METHODS);

  @Test
  public void testAndThen() throws Exception {

    Mockito.doNothing().when(before).accept(Mockito.any());

    before.andThen(after).accept(BiThrowingConsumerTest.DATA);

    final InOrder inOrder = Mockito.inOrder(before, after);

    inOrder.verify(before).accept(BiThrowingConsumerTest.DATA);
    inOrder.verify(after).accept(BiThrowingConsumerTest.DATA);
  }

  @Test
  public void testAndThenThrowsExceptionFromFirstWithoutCallingSecond() throws Exception {
    exception.expect(Matchers.sameInstance(BiThrowingConsumerTest.ERROR));

    Mockito.doThrow(BiThrowingConsumerTest.ERROR).when(before).accept(Mockito.any());

    try {
      before.andThen(after).accept(BiThrowingConsumerTest.DATA);
    } catch (Exception e) {
      Mockito.verify(before).accept(BiThrowingConsumerTest.DATA);
      Mockito.verify(after, Mockito.never()).accept(Mockito.any());
      throw e;
    }
  }

  @Test
  public void testAndThenThrowsExceptionFromSecondAfterCallToFirst() throws Exception {
    exception.expect(Matchers.sameInstance(BiThrowingConsumerTest.ERROR));

    Mockito.doNothing().when(before).accept(Mockito.any());
    Mockito.doThrow(BiThrowingConsumerTest.ERROR).when(after).accept(Mockito.any());

    try {
      before.andThen(after).accept(BiThrowingConsumerTest.DATA);
    } catch (Exception e) {
      final InOrder inOrder = Mockito.inOrder(before, after);

      inOrder.verify(before).accept(BiThrowingConsumerTest.DATA);
      inOrder.verify(after).accept(BiThrowingConsumerTest.DATA);
      throw e;
    }
  }
}

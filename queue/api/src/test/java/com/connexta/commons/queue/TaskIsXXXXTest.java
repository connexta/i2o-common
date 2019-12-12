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
package com.connexta.commons.queue;

import com.connexta.commons.queue.Task.State;
import java.util.Arrays;
import java.util.Collection;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

@RunWith(Parameterized.class)
public class TaskIsXXXXTest {
  @Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {State.PENDING, new boolean[] {false, false, false, false}},
          {State.PROCESSING, new boolean[] {false, false, false, false}},
          {State.FAILED, new boolean[] {true, false, true, false}},
          {State.SUCCESSFUL, new boolean[] {true, true, false, false}},
          {State.UNKNOWN, new boolean[] {false, false, false, true}},
        });
  }

  @Parameter public State state;

  @Parameter(1)
  public boolean[] expected;

  private final Task task = Mockito.mock(Task.class, Mockito.CALLS_REAL_METHODS);

  @Test
  public void testIsCompleted() {
    Mockito.when(task.getState()).thenReturn(state);

    Assert.assertThat(task.isCompleted(), Matchers.equalTo(expected[0]));
  }

  @Test
  public void testIsSuccessful() {
    Mockito.when(task.getState()).thenReturn(state);

    Assert.assertThat(task.isSuccessful(), Matchers.equalTo(expected[1]));
  }

  @Test
  public void testIsFailed() {
    Mockito.when(task.getState()).thenReturn(state);

    Assert.assertThat(task.isFailed(), Matchers.equalTo(expected[2]));
  }

  @Test
  public void testIsUnknown() {
    Mockito.when(task.getState()).thenReturn(state);

    Assert.assertThat(task.isUnknown(), Matchers.equalTo(expected[3]));
  }
}

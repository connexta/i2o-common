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

@RunWith(Parameterized.class)
public class TaskStateReduceTest {

  @Parameters(name = "{0}.reduce({1}) == {2}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {State.PENDING, State.PENDING, State.PENDING},
          {State.PENDING, State.PROCESSING, State.PENDING},
          {State.PENDING, State.FAILED, State.PENDING},
          {State.PENDING, State.SUCCESSFUL, State.PENDING},
          {State.PENDING, State.UNKNOWN, State.PENDING},
          {State.PROCESSING, State.PENDING, State.PENDING},
          {State.PROCESSING, State.SUCCESSFUL, State.PROCESSING},
          {State.PROCESSING, State.PROCESSING, State.PROCESSING},
          {State.PROCESSING, State.FAILED, State.PROCESSING},
          {State.PROCESSING, State.UNKNOWN, State.PROCESSING},
          {State.FAILED, State.PENDING, State.PENDING},
          {State.FAILED, State.SUCCESSFUL, State.FAILED},
          {State.FAILED, State.PROCESSING, State.PROCESSING},
          {State.FAILED, State.FAILED, State.FAILED},
          {State.FAILED, State.UNKNOWN, State.UNKNOWN},
          {State.SUCCESSFUL, State.PENDING, State.PENDING},
          {State.SUCCESSFUL, State.SUCCESSFUL, State.SUCCESSFUL},
          {State.SUCCESSFUL, State.PROCESSING, State.PROCESSING},
          {State.SUCCESSFUL, State.FAILED, State.FAILED},
          {State.SUCCESSFUL, State.UNKNOWN, State.UNKNOWN},
          {State.UNKNOWN, State.PENDING, State.PENDING},
          {State.UNKNOWN, State.SUCCESSFUL, State.UNKNOWN},
          {State.UNKNOWN, State.PROCESSING, State.PROCESSING},
          {State.UNKNOWN, State.FAILED, State.UNKNOWN},
          {State.UNKNOWN, State.UNKNOWN, State.UNKNOWN}
        });
  }

  @Parameter public State state;

  @Parameter(1)
  public State state2;

  @Parameter(2)
  public State expected;

  @Test
  public void reduce() {
    Assert.assertThat(State.reduce(state, state2), Matchers.equalTo(expected));
  }
}

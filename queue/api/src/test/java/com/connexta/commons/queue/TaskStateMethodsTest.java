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
public class TaskStateMethodsTest {
  @Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {State.PENDING, new boolean[] {false, true}},
          {State.PROCESSING, new boolean[] {false, true}},
          {State.FAILED, new boolean[] {true, false}},
          {State.SUCCESSFUL, new boolean[] {true, false}},
          {State.UNKNOWN, new boolean[] {false, false}},
        });
  }

  @Parameter public State state;

  @Parameter(1)
  public boolean[] expectedIsXXXX;

  @Test
  public void testIsFinal() {
    Assert.assertThat(state.isFinal(), Matchers.equalTo(expectedIsXXXX[0]));
  }

  @Test
  public void testIsTransitional() {
    Assert.assertThat(state.isTransitional(), Matchers.equalTo(expectedIsXXXX[1]));
  }
}

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
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TaskStateTest {
  @Test
  public void testFinals() throws Exception {
    Assert.assertThat(
        State.finals().collect(Collectors.toList()),
        Matchers.containsInAnyOrder(State.SUCCESSFUL, State.FAILED));
  }

  @Test
  public void testTransitionals() throws Exception {
    Assert.assertThat(
        State.transitionals().collect(Collectors.toList()),
        Matchers.containsInAnyOrder(State.PENDING, State.PROCESSING));
  }
}

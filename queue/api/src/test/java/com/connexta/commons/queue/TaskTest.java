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

import com.connexta.commons.queue.Task.Constants;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TaskTest {
  @Test
  public void testLimitWhenLessThanMinimum() throws Exception {
    Assert.assertThat(
        Task.limit((byte) (Constants.MIN_PRIORITY - 5)), Matchers.equalTo(Constants.MIN_PRIORITY));
  }

  @Test
  public void testLimitWhenAtTheMinimum() throws Exception {
    Assert.assertThat(Task.limit(Constants.MIN_PRIORITY), Matchers.equalTo(Constants.MIN_PRIORITY));
  }

  @Test
  public void testLimitWhenGreaterThanMaximum() throws Exception {
    Assert.assertThat(
        Task.limit((byte) (Constants.MAX_PRIORITY + (byte) 5)),
        Matchers.equalTo(Constants.MAX_PRIORITY));
  }

  @Test
  public void testLimitWhenAtTheMaximum() throws Exception {
    Assert.assertThat(Task.limit(Constants.MAX_PRIORITY), Matchers.equalTo(Constants.MAX_PRIORITY));
  }

  @Test
  public void testLimitWhenInRange() throws Exception {
    Assert.assertThat(Task.limit((byte) 5), Matchers.equalTo((byte) 5));
  }
}

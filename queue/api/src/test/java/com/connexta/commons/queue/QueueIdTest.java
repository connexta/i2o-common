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

import com.connexta.commons.queue.Queue.Id;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class QueueIdTest {
  private static final Class TASK_CLASS = Task.class;
  private static final String SUB_ID = "id";

  private static final Id ID = Id.of(QueueIdTest.TASK_CLASS, QueueIdTest.SUB_ID);

  @Test
  public void testConstructor() throws Exception {
    Assert.assertThat(QueueIdTest.ID.getTaskClass(), Matchers.equalTo(QueueIdTest.TASK_CLASS));
    Assert.assertThat(QueueIdTest.ID.getId(), Matchers.equalTo(QueueIdTest.SUB_ID));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    Assert.assertThat(
        QueueIdTest.ID.hashCode(),
        Matchers.equalTo(Id.of(QueueIdTest.TASK_CLASS, QueueIdTest.SUB_ID).hashCode()));
  }

  @Test
  public void testHashCodeWhenTaskClassDifferent() throws Exception {
    Assert.assertThat(
        QueueIdTest.ID.hashCode(),
        Matchers.not(
            Matchers.equalTo(
                Id.of(Mockito.mock(Task.class).getClass(), QueueIdTest.SUB_ID).hashCode())));
  }

  @Test
  public void testHashCodeWhenIdDifferent() throws Exception {
    Assert.assertThat(
        QueueIdTest.ID.hashCode(),
        Matchers.not(Matchers.equalTo(Id.of(QueueIdTest.TASK_CLASS, "id2").hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    Assert.assertThat(
        QueueIdTest.ID.equals(Id.of(QueueIdTest.TASK_CLASS, QueueIdTest.SUB_ID)),
        Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(QueueIdTest.ID.equals(QueueIdTest.ID), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(QueueIdTest.ID.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with
                                               something else than expected */)
  @Test
  public void testEqualsWhenNotTheSameClass() throws Exception {
    Assert.assertThat(QueueIdTest.ID.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenTaskClassIsDifferent() throws Exception {
    Assert.assertThat(
        QueueIdTest.ID.equals(Id.of(Mockito.mock(Task.class).getClass(), QueueIdTest.SUB_ID)),
        Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    Assert.assertThat(
        QueueIdTest.ID.equals(Id.of(QueueIdTest.TASK_CLASS, "id2")),
        Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testCompareToWhenEquals() throws Exception {
    Assert.assertThat(
        QueueIdTest.ID.compareTo(Id.of(QueueIdTest.TASK_CLASS, QueueIdTest.SUB_ID)),
        Matchers.equalTo(0));
  }

  @Test
  public void testCompareToWhenEqualsTaskClassIsLess() throws Exception {
    Assert.assertThat(
        QueueIdTest.ID.compareTo(Id.of(Mockito.mock(Task.class).getClass(), QueueIdTest.SUB_ID)),
        Matchers.lessThan(0));
  }

  @Test
  public void testCompareToWhenEqualsTaskClassIsGreater() throws Exception {
    Assert.assertThat(
        Id.of(Mockito.mock(Task.class).getClass(), QueueIdTest.SUB_ID).compareTo(QueueIdTest.ID),
        Matchers.greaterThan(0));
  }

  @Test
  public void testCompareToWhenIdIsLess() throws Exception {
    Assert.assertThat(
        QueueIdTest.ID.compareTo(Id.of(QueueIdTest.TASK_CLASS, "id2")), Matchers.lessThan(0));
  }

  @Test
  public void testCompareToWhenIdIsGreater() throws Exception {
    Assert.assertThat(
        Id.of(QueueIdTest.TASK_CLASS, "id2").compareTo(QueueIdTest.ID), Matchers.greaterThan(0));
  }
}

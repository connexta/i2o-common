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
package com.connexta.commons.queue.impl.persistence;

import com.connexta.commons.queue.impl.persistence.unknown.UnknownTaskInfoPojo;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class AbstractTaskInfoPojoTest {
  private static final int VERSION = 1;
  private static final UUID ID = UUID.randomUUID();
  private static final int BASE_VERSION = 2;
  private static final byte PRIORITY = 3;

  private static final TestAbstractTaskInfoPojo POJO =
      new TestAbstractTaskInfoPojo()
          .setVersion(AbstractTaskInfoPojoTest.VERSION)
          .setId(AbstractTaskInfoPojoTest.ID)
          .setBaseVersion(AbstractTaskInfoPojoTest.BASE_VERSION)
          .setPriority(AbstractTaskInfoPojoTest.PRIORITY);

  private final TestAbstractTaskInfoPojo pojo2 =
      new TestAbstractTaskInfoPojo()
          .setVersion(AbstractTaskInfoPojoTest.VERSION)
          .setId(AbstractTaskInfoPojoTest.ID)
          .setBaseVersion(AbstractTaskInfoPojoTest.BASE_VERSION)
          .setPriority(AbstractTaskInfoPojoTest.PRIORITY);

  @Test
  public void testSetAndGetId() throws Exception {
    final TestAbstractTaskInfoPojo pojo =
        new TestAbstractTaskInfoPojo().setId(AbstractTaskInfoPojoTest.ID);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(AbstractTaskInfoPojoTest.ID));
  }

  @Test
  public void testSetAndGetVersion() throws Exception {
    final TestAbstractTaskInfoPojo pojo =
        new TestAbstractTaskInfoPojo().setVersion(AbstractTaskInfoPojoTest.VERSION);

    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(AbstractTaskInfoPojoTest.VERSION));
  }

  @Test
  public void testSetAndGetBaseVersion() throws Exception {
    final TestAbstractTaskInfoPojo pojo =
        new TestAbstractTaskInfoPojo().setBaseVersion(AbstractTaskInfoPojoTest.BASE_VERSION);

    Assert.assertThat(
        pojo.getBaseVersion(), Matchers.equalTo(AbstractTaskInfoPojoTest.BASE_VERSION));
  }

  @Test
  public void testSetAndGetPriority() throws Exception {
    final TestAbstractTaskInfoPojo pojo =
        new TestAbstractTaskInfoPojo().setPriority(AbstractTaskInfoPojoTest.PRIORITY);

    Assert.assertThat(pojo.getPriority(), Matchers.equalTo(AbstractTaskInfoPojoTest.PRIORITY));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    Assert.assertThat(AbstractTaskInfoPojoTest.POJO.hashCode(), Matchers.equalTo(pojo2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    pojo2.setId(UUID.randomUUID());

    Assert.assertThat(
        AbstractTaskInfoPojoTest.POJO.hashCode(), Matchers.not(Matchers.equalTo(pojo2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    Assert.assertThat(AbstractTaskInfoPojoTest.POJO.equals(pojo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(
        AbstractTaskInfoPojoTest.POJO.equals(AbstractTaskInfoPojoTest.POJO),
        Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(AbstractTaskInfoPojoTest.POJO.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotAAbstractTaskInfoPojo() throws Exception {
    Assert.assertThat(AbstractTaskInfoPojoTest.POJO.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    pojo2.setId(UUID.randomUUID());

    Assert.assertThat(
        AbstractTaskInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenVersionIsDifferent() throws Exception {
    pojo2.setVersion(AbstractTaskInfoPojoTest.VERSION + 2);

    Assert.assertThat(
        AbstractTaskInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenBaseVersionIsDifferent() throws Exception {
    pojo2.setBaseVersion(AbstractTaskInfoPojoTest.BASE_VERSION + 2);

    Assert.assertThat(
        AbstractTaskInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenPriorityIsDifferent() throws Exception {
    pojo2.setPriority((byte) (AbstractTaskInfoPojoTest.PRIORITY + 2));

    Assert.assertThat(
        AbstractTaskInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testUnknownEqualsPojo() throws Exception {
    final AbstractTaskInfoPojo unknown =
        new UnknownTaskInfoPojo()
            .setVersion(AbstractTaskInfoPojoTest.VERSION)
            .setId(AbstractTaskInfoPojoTest.ID)
            .setBaseVersion(AbstractTaskInfoPojoTest.BASE_VERSION)
            .setPriority(AbstractTaskInfoPojoTest.PRIORITY);

    Assert.assertThat(unknown.equals(AbstractTaskInfoPojoTest.POJO), Matchers.equalTo(true));
    Assert.assertThat(AbstractTaskInfoPojoTest.POJO.equals(unknown), Matchers.equalTo(true));
  }
}

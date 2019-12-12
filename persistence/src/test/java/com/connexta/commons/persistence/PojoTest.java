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
package com.connexta.commons.persistence;

import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class PojoTest {
  private static final UUID ID = UUID.randomUUID();
  private static final int VERSION = 3;

  private static final Pojo<?> POJO =
      Mockito.mock(
              Pojo.class,
              Mockito.withSettings().useConstructor().defaultAnswer(Mockito.CALLS_REAL_METHODS))
          .setId(PojoTest.ID)
          .setVersion(PojoTest.VERSION);

  private final Pojo<?> pojo2 =
      Mockito.mock(
              Pojo.class,
              Mockito.withSettings().useConstructor().defaultAnswer(Mockito.CALLS_REAL_METHODS))
          .setId(PojoTest.ID)
          .setVersion(PojoTest.VERSION);

  @Test
  public void testCtor() throws Exception {
    final Pojo<?> pojo =
        Mockito.mock(
            Pojo.class,
            Mockito.withSettings().useConstructor().defaultAnswer(Mockito.CALLS_REAL_METHODS));

    Assert.assertThat(pojo.getId(), Matchers.nullValue());
    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(0));
  }

  @Test
  public void testGetSetId() throws Exception {
    final Pojo<?> pojo =
        Mockito.mock(
            Pojo.class,
            Mockito.withSettings().useConstructor().defaultAnswer(Mockito.CALLS_REAL_METHODS));

    pojo.setId(PojoTest.ID);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(PojoTest.ID));
  }

  @Test
  public void testGetSetVersion() throws Exception {
    final Pojo<?> pojo =
        Mockito.mock(
            Pojo.class,
            Mockito.withSettings().useConstructor().defaultAnswer(Mockito.CALLS_REAL_METHODS));

    pojo.setVersion(PojoTest.VERSION);

    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(PojoTest.VERSION));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    Assert.assertThat(PojoTest.POJO.hashCode0(), Matchers.equalTo(pojo2.hashCode0()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    pojo2.setId(UUID.randomUUID());

    Assert.assertThat(PojoTest.POJO.hashCode0(), Matchers.not(Matchers.equalTo(pojo2.hashCode0())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    Assert.assertThat(PojoTest.POJO.equals0(pojo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(PojoTest.POJO.equals0(PojoTest.POJO), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals0() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(PojoTest.POJO.equals0(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals0() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotARequestInfoPojo() throws Exception {
    Assert.assertThat(PojoTest.POJO.equals0("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    pojo2.setId(UUID.randomUUID());

    Assert.assertThat(PojoTest.POJO.equals0(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenVersionIsDifferent() throws Exception {
    pojo2.setVersion(PojoTest.VERSION + 2);

    Assert.assertThat(PojoTest.POJO.equals0(pojo2), Matchers.not(Matchers.equalTo(true)));
  }
}

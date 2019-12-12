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

import com.connexta.commons.queue.Task.State;
import com.connexta.commons.queue.impl.persistence.unknown.UnknownTaskPojo;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class AbstractTaskPojoTest {
  private static final int VERSION = 1;
  private static final int BASE_VERSION = 2;
  private static final UUID ID = UUID.randomUUID();
  private static final byte PRIORITY = 3;
  private static final Instant ORIGINAL_QUEUED_TIME = Instant.ofEpochMilli(10L);
  private static final int ATTEMPTS = 5;
  private static final Instant QUEUED_TIME = Instant.ofEpochMilli(10000L);
  private static final String STATE = State.PENDING.name();
  private static final State STATE2 = State.PENDING;
  private static final State STATE3 = State.PROCESSING;
  private static final long FROZEN_TIME = 23453L;
  private static final Duration DURATION = Duration.ofMinutes(20L);
  private static final Duration DURATION2 = Duration.ofMillis(12L);
  private static final Duration DURATION3 = Duration.ofMillis(5L);
  private static final Map<String, Duration> DURATIONS =
      Map.of(
          AbstractTaskPojoTest.STATE2.name(),
          AbstractTaskPojoTest.DURATION2,
          AbstractTaskPojoTest.STATE3.name(),
          AbstractTaskPojoTest.DURATION3);

  private static final TestAbstractTaskPojo POJO =
      new TestAbstractTaskPojo()
          .setVersion(AbstractTaskPojoTest.VERSION)
          .setBaseVersion(AbstractTaskPojoTest.BASE_VERSION)
          .setId(AbstractTaskPojoTest.ID)
          .setPriority(AbstractTaskPojoTest.PRIORITY)
          .setOriginalQueuedTime(AbstractTaskPojoTest.ORIGINAL_QUEUED_TIME)
          .setAttempts(AbstractTaskPojoTest.ATTEMPTS)
          .setQueuedTime(AbstractTaskPojoTest.QUEUED_TIME)
          .setState(AbstractTaskPojoTest.STATE)
          .setFrozenTime(AbstractTaskPojoTest.FROZEN_TIME)
          .setDuration(AbstractTaskPojoTest.DURATION)
          .setDurations(AbstractTaskPojoTest.DURATIONS);

  private final TestAbstractTaskPojo pojo2 =
      new TestAbstractTaskPojo()
          .setVersion(AbstractTaskPojoTest.VERSION)
          .setBaseVersion(AbstractTaskPojoTest.BASE_VERSION)
          .setId(AbstractTaskPojoTest.ID)
          .setPriority(AbstractTaskPojoTest.PRIORITY)
          .setOriginalQueuedTime(AbstractTaskPojoTest.ORIGINAL_QUEUED_TIME)
          .setAttempts(AbstractTaskPojoTest.ATTEMPTS)
          .setQueuedTime(AbstractTaskPojoTest.QUEUED_TIME)
          .setState(AbstractTaskPojoTest.STATE)
          .setFrozenTime(AbstractTaskPojoTest.FROZEN_TIME)
          .setDuration(AbstractTaskPojoTest.DURATION)
          .setDurations(AbstractTaskPojoTest.DURATIONS);

  @Test
  public void testSetAndGetId() throws Exception {
    final TestAbstractTaskPojo pojo = new TestAbstractTaskPojo().setId(AbstractTaskPojoTest.ID);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(AbstractTaskPojoTest.ID));
  }

  @Test
  public void testSetAndGetVersion() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo().setVersion(AbstractTaskPojoTest.VERSION);

    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(AbstractTaskPojoTest.VERSION));
  }

  @Test
  public void testSetAndGetBaseVersion() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo().setBaseVersion(AbstractTaskPojoTest.BASE_VERSION);

    Assert.assertThat(pojo.getBaseVersion(), Matchers.equalTo(AbstractTaskPojoTest.BASE_VERSION));
  }

  @Test
  public void testSetAndGetPriority() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo().setPriority(AbstractTaskPojoTest.PRIORITY);

    Assert.assertThat(pojo.getPriority(), Matchers.equalTo(AbstractTaskPojoTest.PRIORITY));
  }

  @Test
  public void testSetAndGetOriginalQueuedTime() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo().setOriginalQueuedTime(AbstractTaskPojoTest.ORIGINAL_QUEUED_TIME);

    Assert.assertThat(
        pojo.getOriginalQueuedTime(), Matchers.equalTo(AbstractTaskPojoTest.ORIGINAL_QUEUED_TIME));
  }

  @Test
  public void testSetAndGetTotalAttempts() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo().setAttempts(AbstractTaskPojoTest.ATTEMPTS);

    Assert.assertThat(pojo.getAttempts(), Matchers.equalTo(AbstractTaskPojoTest.ATTEMPTS));
  }

  @Test
  public void testSetAndGetQueuedTime() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo().setQueuedTime(AbstractTaskPojoTest.QUEUED_TIME);

    Assert.assertThat(pojo.getQueuedTime(), Matchers.equalTo(AbstractTaskPojoTest.QUEUED_TIME));
  }

  @Test
  public void testSetAndGetState() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo().setState(AbstractTaskPojoTest.STATE);

    Assert.assertThat(pojo.getState(), Matchers.equalTo(AbstractTaskPojoTest.STATE));
  }

  @Test
  public void testSetAndGetStateWithEnum() throws Exception {
    final TestAbstractTaskPojo pojo = new TestAbstractTaskPojo().setState(State.PROCESSING);

    Assert.assertThat(pojo.getState(), Matchers.equalTo(State.PROCESSING.name()));
  }

  @Test
  public void testSetAndGetFrozenTime() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo().setFrozenTime(AbstractTaskPojoTest.FROZEN_TIME);

    Assert.assertThat(pojo.getFrozenTime(), Matchers.equalTo(AbstractTaskPojoTest.FROZEN_TIME));
  }

  @Test
  public void testSetAndGetDuration() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo().setDuration(AbstractTaskPojoTest.DURATION);

    Assert.assertThat(pojo.getDuration(), Matchers.equalTo(AbstractTaskPojoTest.DURATION));
  }

  @Test
  public void testSetAndGetDurations() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo().setDurations(AbstractTaskPojoTest.DURATIONS);

    Assert.assertThat(pojo.getDurations(), Matchers.equalTo(AbstractTaskPojoTest.DURATIONS));
  }

  @Test
  public void testSetAndGetDurationsWithNull() throws Exception {
    final TestAbstractTaskPojo pojo = new TestAbstractTaskPojo().setDurations(null);

    Assert.assertThat(pojo.getDurations(), Matchers.anEmptyMap());
  }

  @Test
  public void testDurations() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo().setDurations(AbstractTaskPojoTest.DURATIONS);

    Assert.assertThat(
        pojo.durations().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
        Matchers.equalTo(AbstractTaskPojoTest.DURATIONS));
  }

  @Test
  public void testSetDurationForState() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo()
            .setDuration(AbstractTaskPojoTest.STATE2.name(), AbstractTaskPojoTest.DURATION2)
            .setDuration(AbstractTaskPojoTest.STATE3.name(), AbstractTaskPojoTest.DURATION3);

    Assert.assertThat(pojo.getDurations(), Matchers.equalTo(AbstractTaskPojoTest.DURATIONS));
  }

  @Test
  public void testSetDurationForStateAsEnum() throws Exception {
    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo()
            .setDuration(AbstractTaskPojoTest.STATE2, AbstractTaskPojoTest.DURATION2)
            .setDuration(AbstractTaskPojoTest.STATE3, AbstractTaskPojoTest.DURATION3);

    Assert.assertThat(pojo.getDurations(), Matchers.equalTo(AbstractTaskPojoTest.DURATIONS));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    Assert.assertThat(AbstractTaskPojoTest.POJO.hashCode(), Matchers.equalTo(pojo2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    pojo2.setId(UUID.randomUUID());

    Assert.assertThat(
        AbstractTaskPojoTest.POJO.hashCode(), Matchers.not(Matchers.equalTo(pojo2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    Assert.assertThat(AbstractTaskPojoTest.POJO.equals(pojo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(
        AbstractTaskPojoTest.POJO.equals(AbstractTaskPojoTest.POJO), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(AbstractTaskPojoTest.POJO.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotATaskPojo() throws Exception {
    Assert.assertThat(AbstractTaskPojoTest.POJO.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    pojo2.setId(UUID.randomUUID());

    Assert.assertThat(
        AbstractTaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenVersionIsDifferent() throws Exception {
    pojo2.setVersion(AbstractTaskPojoTest.VERSION + 2);

    Assert.assertThat(
        AbstractTaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenBaseVersionIsDifferent() throws Exception {
    pojo2.setBaseVersion(AbstractTaskPojoTest.BASE_VERSION + 2);

    Assert.assertThat(
        AbstractTaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenPriorityIsDifferent() throws Exception {
    pojo2.setPriority((byte) (AbstractTaskPojoTest.PRIORITY + 2));

    Assert.assertThat(
        AbstractTaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenOriginalQueuedTimeIsDifferent() throws Exception {
    pojo2.setOriginalQueuedTime(AbstractTaskPojoTest.ORIGINAL_QUEUED_TIME.plusMillis(2L));
    Assert.assertThat(
        AbstractTaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenAttemptsIsDifferent() throws Exception {
    pojo2.setAttempts(AbstractTaskPojoTest.ATTEMPTS + 2);

    Assert.assertThat(
        AbstractTaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenQueuedTimeIsDifferent() throws Exception {
    pojo2.setQueuedTime(AbstractTaskPojoTest.QUEUED_TIME.plusMillis(2L));

    Assert.assertThat(
        AbstractTaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenStateIsDifferent() throws Exception {
    pojo2.setState(AbstractTaskPojoTest.STATE + "2");

    Assert.assertThat(
        AbstractTaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenFrozenTimeTimeIsDifferent() throws Exception {
    pojo2.setFrozenTime(AbstractTaskPojoTest.FROZEN_TIME + 2L);

    Assert.assertThat(
        AbstractTaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDurationIsDifferent() throws Exception {
    pojo2.setDuration(AbstractTaskPojoTest.DURATION.plusMillis(2L));

    Assert.assertThat(
        AbstractTaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDurationsAreDifferent() throws Exception {
    pojo2.setDurations(Map.of(AbstractTaskPojoTest.STATE2.name(), AbstractTaskPojoTest.DURATION2));

    Assert.assertThat(
        AbstractTaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testUnknownEqualsPojo() throws Exception {
    final AbstractTaskPojo unknown =
        new UnknownTaskPojo()
            .setVersion(AbstractTaskPojoTest.VERSION)
            .setBaseVersion(AbstractTaskPojoTest.BASE_VERSION)
            .setId(AbstractTaskPojoTest.ID)
            .setPriority(AbstractTaskPojoTest.PRIORITY)
            .setOriginalQueuedTime(AbstractTaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setAttempts(AbstractTaskPojoTest.ATTEMPTS)
            .setQueuedTime(AbstractTaskPojoTest.QUEUED_TIME)
            .setState(AbstractTaskPojoTest.STATE)
            .setFrozenTime(AbstractTaskPojoTest.FROZEN_TIME)
            .setDuration(AbstractTaskPojoTest.DURATION)
            .setDurations(AbstractTaskPojoTest.DURATIONS);

    Assert.assertThat(unknown.equals(AbstractTaskPojoTest.POJO), Matchers.equalTo(true));
    Assert.assertThat(AbstractTaskPojoTest.POJO.equals(unknown), Matchers.equalTo(true));
  }
}

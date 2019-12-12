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
package com.connexta.commons.queue.impl;

import com.connexta.commons.queue.Task;
import com.connexta.commons.queue.Task.State;
import com.connexta.commons.queue.impl.persistence.AbstractTaskPojo;
import com.connexta.commons.queue.impl.persistence.TestAbstractTaskPojo;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class AbstractTaskTest {
  private static final UUID ID = UUID.randomUUID();
  private static final byte PRIORITY = 5;
  private static final int ATTEMPTS = 3;
  private static final State STATE = State.FAILED;
  private static final TestErrorCode FAILURE_REASON = TestErrorCode.FAILURE;
  private static final String FAILURE_MSG = "some message";

  private static final Duration DURATION2 = Duration.ofSeconds(5L);
  private static final Duration DURATION3 = Duration.ofSeconds(8L);
  private static final Duration DURATION4 = Duration.ofSeconds(17L);
  private static final Duration DURATION5 = Duration.ofSeconds(24L);
  private static final long NANOS = 1234000000L;
  private static final long NANOS2 = AbstractTaskTest.NANOS + AbstractTaskTest.DURATION2.toNanos();
  private static final long NANOS3 = AbstractTaskTest.NANOS2 + AbstractTaskTest.DURATION3.toNanos();
  private static final long NANOS4 = AbstractTaskTest.NANOS3 + AbstractTaskTest.DURATION4.toNanos();
  private static final long NANOS5 = AbstractTaskTest.NANOS4 + AbstractTaskTest.DURATION5.toNanos();
  private static final Instant NOW = Instant.ofEpochMilli(1234L);
  private static final Instant THEN2 = AbstractTaskTest.NOW.plusNanos(AbstractTaskTest.NANOS2);
  private static final Instant THEN3 = AbstractTaskTest.NOW.plusNanos(AbstractTaskTest.NANOS3);
  private static final Instant THEN4 = AbstractTaskTest.NOW.plusNanos(AbstractTaskTest.NANOS4);
  private static final Instant THEN5 = AbstractTaskTest.NOW.plusNanos(AbstractTaskTest.NANOS5);

  private static final TestTaskInfo INFO = new TestTaskInfo(AbstractTaskTest.PRIORITY);

  private static final TestAbstractTask TASK =
      new TestAbstractTask(
          AbstractTaskTest.INFO,
          AbstractTaskTest.mockClock(
              AbstractTaskTest.NOW,
              AbstractTaskTest.THEN5,
              AbstractTaskTest.NANOS,
              AbstractTaskTest.NANOS5));

  static {
    AbstractTaskTest.TASK.setId(AbstractTaskTest.ID);
    AbstractTaskTest.TASK.setAttempts(AbstractTaskTest.ATTEMPTS);
    AbstractTaskTest.TASK.setQueuedTime(AbstractTaskTest.THEN2);
    AbstractTaskTest.TASK.setState(AbstractTaskTest.STATE);
    AbstractTaskTest.TASK.setFailureReason(AbstractTaskTest.FAILURE_REASON);
    AbstractTaskTest.TASK.setFailureMessage(AbstractTaskTest.FAILURE_MSG);
    AbstractTaskTest.TASK.setState(AbstractTaskTest.STATE);
    AbstractTaskTest.TASK.setThawedTime(AbstractTaskTest.NANOS5);
    AbstractTaskTest.TASK.setDuration(AbstractTaskTest.DURATION4);
    AbstractTaskTest.TASK.setDurations(
        Map.of(
            State.PENDING,
            AbstractTaskTest.DURATION2,
            State.PROCESSING,
            AbstractTaskTest.DURATION3));
  }

  @Rule public ExpectedException exception = ExpectedException.none();

  private final TestAbstractTask task2 =
      new TestAbstractTask(
          AbstractTaskTest.INFO,
          AbstractTaskTest.mockClock(
              AbstractTaskTest.NOW,
              AbstractTaskTest.THEN5,
              AbstractTaskTest.NANOS,
              AbstractTaskTest.NANOS5));

  private final TestAbstractTaskPojo pojo =
      new TestAbstractTaskPojo()
          .setBaseVersion(AbstractTaskPojo.CURRENT_BASE_VERSION)
          .setVersion(TestAbstractTask.CURRENT_VERSION)
          .setId(AbstractTaskTest.ID)
          .setPriority(AbstractTaskTest.PRIORITY)
          .setOriginalQueuedTime(AbstractTaskTest.NOW)
          .setAttempts(AbstractTaskTest.ATTEMPTS)
          .setQueuedTime(AbstractTaskTest.THEN2)
          .setState(AbstractTaskTest.STATE)
          .setDuration(AbstractTaskTest.DURATION4)
          .setDuration(State.PENDING, AbstractTaskTest.DURATION2)
          .setDuration(State.PROCESSING, AbstractTaskTest.DURATION3)
          .setFrozenTime(AbstractTaskTest.NANOS);

  public AbstractTaskTest() {
    task2.setId(AbstractTaskTest.ID);
    task2.setAttempts(AbstractTaskTest.ATTEMPTS);
    task2.setQueuedTime(AbstractTaskTest.THEN2);
    task2.setState(AbstractTaskTest.STATE);
    task2.setFailureReason(AbstractTaskTest.FAILURE_REASON);
    task2.setFailureMessage(AbstractTaskTest.FAILURE_MSG);
    task2.setState(AbstractTaskTest.STATE);
    task2.setThawedTime(AbstractTaskTest.NANOS5);
    task2.setDuration(AbstractTaskTest.DURATION4);
    task2.setDurations(
        Map.of(
            State.PENDING,
            AbstractTaskTest.DURATION2,
            State.PROCESSING,
            AbstractTaskTest.DURATION3));
  }

  @Test
  public void testConstructor() throws Exception {
    final Clock clock =
        AbstractTaskTest.mockClock(
            AbstractTaskTest.NOW,
            AbstractTaskTest.NANOS, // thawedTime
            AbstractTaskTest.NANOS2, // getDuration()
            AbstractTaskTest.NANOS3); // getDuration(PENDING)

    final TestAbstractTask task = new TestAbstractTask(AbstractTaskTest.INFO, clock);

    Assert.assertThat(task.getPriority(), Matchers.equalTo(AbstractTaskTest.PRIORITY));
    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.getFailureReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getDuration(), Matchers.equalTo(AbstractTaskTest.DURATION2));
    Assert.assertThat(task.getDurations(), Matchers.equalTo(Collections.emptyMap()));
    Assert.assertThat(
        task.getDuration(State.PENDING),
        Matchers.equalTo(AbstractTaskTest.DURATION2.plus(AbstractTaskTest.DURATION3)));
    Assert.assertThat(task.getDuration(State.PROCESSING), Matchers.equalTo(Duration.ZERO));
    Assert.assertThat(task.getThawedTime(), Matchers.equalTo(AbstractTaskTest.NANOS));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(false));
  }

  @Test
  public void testConstructorWithPojo() throws Exception {
    final Clock clock = AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5);

    final TestAbstractTask task = new TestAbstractTask(pojo, clock);

    Assert.assertThat(task.getPriority(), Matchers.equalTo(AbstractTaskTest.PRIORITY));
    Assert.assertThat(task.getAttempts(), Matchers.equalTo(AbstractTaskTest.ATTEMPTS));
    Assert.assertThat(task.getState(), Matchers.equalTo(AbstractTaskTest.STATE));
    Assert.assertThat(task.getFailureReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(AbstractTaskTest.THEN2));
    Assert.assertThat(task.getDuration(), Matchers.equalTo(AbstractTaskTest.DURATION4));
    Assert.assertThat(
        task.getDurations(),
        Matchers.equalTo(
            Map.of(
                State.PENDING,
                AbstractTaskTest.DURATION2,
                State.PROCESSING,
                AbstractTaskTest.DURATION3)));
    Assert.assertThat(
        task.getDuration(State.PENDING), Matchers.equalTo(AbstractTaskTest.DURATION2));
    Assert.assertThat(
        task.getDuration(State.PROCESSING), Matchers.equalTo(AbstractTaskTest.DURATION3));
    Assert.assertThat(task.getThawedTime(), Matchers.equalTo(AbstractTaskTest.NANOS5));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(false));
  }

  @Test
  public void testHasUnknownWhenStateIsUnknown() throws Exception {
    pojo.setState("new state");

    final Clock clock = AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5);

    final TestAbstractTask task = new TestAbstractTask(pojo, clock);

    Assert.assertThat(task.getState(), Matchers.equalTo(State.UNKNOWN));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(true));
  }

  @Test
  public void testSetStateWhenAlreadyCompleted() throws Exception {
    final Clock clock = AbstractTaskTest.mockClock(AbstractTaskTest.NOW, AbstractTaskTest.NANOS);

    final TestAbstractTask task = new TestAbstractTask(AbstractTaskTest.INFO, clock);

    task.setState(State.SUCCESSFUL);

    task.setState(State.FAILED, AbstractTaskTest.FAILURE_REASON, AbstractTaskTest.FAILURE_MSG);

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.SUCCESSFUL));
    Assert.assertThat(task.getFailureReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
  }

  @Test
  public void testSetStateToFailFromProcessing() throws Exception {
    final Clock clock =
        AbstractTaskTest.mockClock(
            AbstractTaskTest.NOW, AbstractTaskTest.NANOS, AbstractTaskTest.NANOS2);

    final TestAbstractTask task = new TestAbstractTask(AbstractTaskTest.INFO, clock);

    task.setState(State.PROCESSING);

    task.setState(State.FAILED, AbstractTaskTest.FAILURE_REASON, AbstractTaskTest.FAILURE_MSG);

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.FAILED));
    Assert.assertThat(
        task.getFailureReason(), OptionalMatchers.isPresentAndIs(AbstractTaskTest.FAILURE_REASON));
    Assert.assertThat(
        task.getFailureMessage(), OptionalMatchers.isPresentAndIs(AbstractTaskTest.FAILURE_MSG));
    Assert.assertThat(task.getDuration(), Matchers.equalTo(AbstractTaskTest.DURATION2));
    Assert.assertThat(task.getDuration(State.PENDING), Matchers.equalTo(Duration.ZERO));
    Assert.assertThat(
        task.getDuration(State.PROCESSING), Matchers.equalTo(AbstractTaskTest.DURATION2));
  }

  @Test
  public void testSetStateToFailFromPending() throws Exception {
    final Clock clock =
        AbstractTaskTest.mockClock(
            AbstractTaskTest.NOW, AbstractTaskTest.NANOS, AbstractTaskTest.NANOS2);

    final TestAbstractTask task = new TestAbstractTask(AbstractTaskTest.INFO, clock);

    task.setState(State.PENDING);

    task.setState(State.SUCCESSFUL, null, null);

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.SUCCESSFUL));
    Assert.assertThat(task.getFailureReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getDuration(), Matchers.equalTo(AbstractTaskTest.DURATION2));
    Assert.assertThat(
        task.getDuration(State.PENDING), Matchers.equalTo(AbstractTaskTest.DURATION2));
    Assert.assertThat(task.getDuration(State.PROCESSING), Matchers.equalTo(Duration.ZERO));
  }

  @Test
  public void testSetStateToPendingFromProcessingWithRetryableError() throws Exception {
    final Clock clock =
        AbstractTaskTest.mockClock(
            AbstractTaskTest.NOW,
            AbstractTaskTest.THEN2,
            AbstractTaskTest.NANOS,
            AbstractTaskTest.NANOS2,
            AbstractTaskTest.NANOS3,
            AbstractTaskTest.NANOS4);

    final TestAbstractTask task = new TestAbstractTask(AbstractTaskTest.INFO, clock);

    task.setState(State.PROCESSING);

    task.setState(State.PENDING, TestErrorCode.RETRYABLE_FAILURE, AbstractTaskTest.FAILURE_MSG);

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(2));
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(AbstractTaskTest.THEN2));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(
        task.getFailureReason(), OptionalMatchers.isPresentAndIs(TestErrorCode.RETRYABLE_FAILURE));
    Assert.assertThat(
        task.getFailureMessage(), OptionalMatchers.isPresentAndIs(AbstractTaskTest.FAILURE_MSG));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(AbstractTaskTest.DURATION2.plus(AbstractTaskTest.DURATION3)));
    Assert.assertThat(
        task.getDuration(State.PENDING),
        Matchers.equalTo(AbstractTaskTest.DURATION3.plus(AbstractTaskTest.DURATION4)));
    Assert.assertThat(
        task.getDuration(State.PROCESSING), Matchers.equalTo(AbstractTaskTest.DURATION2));
  }

  @Test
  public void testSetStateToPendingFromProcessingWithNonRetryableError() throws Exception {
    final Clock clock =
        AbstractTaskTest.mockClock(
            AbstractTaskTest.NOW,
            AbstractTaskTest.NANOS,
            AbstractTaskTest.NANOS2,
            AbstractTaskTest.NANOS3,
            AbstractTaskTest.NANOS4);

    final TestAbstractTask task = new TestAbstractTask(AbstractTaskTest.INFO, clock);

    task.setState(State.PROCESSING);

    task.setState(State.PENDING, AbstractTaskTest.FAILURE_REASON, AbstractTaskTest.FAILURE_MSG);

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.getFailureReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(AbstractTaskTest.DURATION2.plus(AbstractTaskTest.DURATION3)));
    Assert.assertThat(
        task.getDuration(State.PENDING),
        Matchers.equalTo(AbstractTaskTest.DURATION3.plus(AbstractTaskTest.DURATION4)));
    Assert.assertThat(
        task.getDuration(State.PROCESSING), Matchers.equalTo(AbstractTaskTest.DURATION2));
  }

  @Test
  public void testSetStateToPendingFromProcessingWitNoError() throws Exception {
    final Clock clock =
        AbstractTaskTest.mockClock(
            AbstractTaskTest.NOW,
            AbstractTaskTest.NANOS,
            AbstractTaskTest.NANOS2,
            AbstractTaskTest.NANOS3,
            AbstractTaskTest.NANOS4);

    final TestAbstractTask task = new TestAbstractTask(AbstractTaskTest.INFO, clock);

    task.setState(State.PROCESSING);

    task.setState(State.PENDING, null, null);

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(AbstractTaskTest.NOW));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.getFailureReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(AbstractTaskTest.DURATION2.plus(AbstractTaskTest.DURATION3)));
    Assert.assertThat(
        task.getDuration(State.PENDING),
        Matchers.equalTo(AbstractTaskTest.DURATION3.plus(AbstractTaskTest.DURATION4)));
    Assert.assertThat(
        task.getDuration(State.PROCESSING), Matchers.equalTo(AbstractTaskTest.DURATION2));
  }

  @Test
  public void testWriteTo() throws Exception {
    pojo.setFrozenTime(AbstractTaskTest.THEN5.toEpochMilli());

    final TestAbstractTaskPojo pojo2 = new TestAbstractTaskPojo();

    AbstractTaskTest.TASK.writeTo(pojo2);

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
  }

  @Test
  public void testWriteToWhenOriginalQueuedTimeIsNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*originalQueuedTime.*"));

    task2.setOriginalQueuedTime(null);

    final TestAbstractTaskPojo pojo2 = new TestAbstractTaskPojo();

    task2.writeTo(pojo2);
  }

  @Test
  public void testWriteToWhenQueuedTimeIsNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*queuedTime.*"));

    task2.setQueuedTime(null);

    final TestAbstractTaskPojo pojo2 = new TestAbstractTaskPojo();

    task2.writeTo(pojo2);
  }

  @Test
  public void testWriteToWhenStateIsNull() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*state.*"));

    task2.setState(null);

    final TestAbstractTaskPojo pojo2 = new TestAbstractTaskPojo();

    task2.writeTo(pojo2);
  }

  @Test
  public void testWriteToWhenHasUnknowns() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*unknown.*task.*"));

    task2.setState(State.UNKNOWN);

    final TestAbstractTaskPojo pojo2 = new TestAbstractTaskPojo();

    task2.writeTo(pojo);
  }

  @Test
  public void testReadFromCurrentVersion() throws Exception {
    task2.setThawedTime(AbstractTaskTest.NANOS5);
    task2.setFailureReason(null);
    task2.setFailureMessage(null);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);

    Assert.assertThat(task.equals0(task2), Matchers.equalTo(true));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(false));
  }

  @Test
  public void testReadFromFutureVersion() throws Exception {
    pojo.setBaseVersion(9999999);
    task2.setThawedTime(AbstractTaskTest.NANOS5);
    task2.setFailureReason(null);
    task2.setFailureMessage(null);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);

    Assert.assertThat(task.equals0(task2), Matchers.equalTo(true));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(false));
  }

  @Test
  public void testReadFromUnsupportedVersion() throws Exception {
    exception.expect(TestUnsupportedVersionException.class);
    exception.expectMessage(Matchers.matchesPattern(".*unsupported.*base version.*"));

    pojo.setBaseVersion(-1);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithLessThanMinPriority() throws Exception {
    task2.setThawedTime(AbstractTaskTest.NANOS5);
    task2.setFailureReason(null);
    task2.setFailureMessage(null);
    pojo.setPriority((byte) (Task.Constants.MIN_PRIORITY - 5));
    task2.setPriority(Task.Constants.MIN_PRIORITY);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);

    Assert.assertThat(task.equals0(task2), Matchers.equalTo(true));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(false));
  }

  @Test
  public void testReadFromCurrentVersionWithMinPriority() throws Exception {
    task2.setThawedTime(AbstractTaskTest.NANOS5);
    task2.setFailureReason(null);
    task2.setFailureMessage(null);
    pojo.setPriority(Task.Constants.MIN_PRIORITY);
    task2.setPriority(Task.Constants.MIN_PRIORITY);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);

    Assert.assertThat(task.equals0(task2), Matchers.equalTo(true));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(false));
  }

  @Test
  public void testReadFromCurrentVersionWithMoreThanMaxPriority() throws Exception {
    task2.setThawedTime(AbstractTaskTest.NANOS5);
    task2.setFailureReason(null);
    task2.setFailureMessage(null);
    pojo.setPriority((byte) (Task.Constants.MAX_PRIORITY + 5));
    task2.setPriority(Task.Constants.MAX_PRIORITY);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);

    Assert.assertThat(task.equals0(task2), Matchers.equalTo(true));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(false));
  }

  @Test
  public void testReadFromCurrentVersionWithMaxPriority() throws Exception {
    task2.setThawedTime(AbstractTaskTest.NANOS5);
    task2.setFailureReason(null);
    task2.setFailureMessage(null);
    pojo.setPriority(Task.Constants.MAX_PRIORITY);
    task2.setPriority(Task.Constants.MAX_PRIORITY);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);

    Assert.assertThat(task.equals0(task2), Matchers.equalTo(true));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(false));
  }

  @Test
  public void testReadFromCurrentVersionWithNullOriginalQueuedTime() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*originalQueuedTime.*"));

    task2.setThawedTime(AbstractTaskTest.NANOS5);
    pojo.setOriginalQueuedTime(null);
    task2.setOriginalQueuedTime(null);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithNullQueuedTime() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*queuedTime.*"));

    task2.setThawedTime(AbstractTaskTest.NANOS5);
    pojo.setQueuedTime(null);
    task2.setQueuedTime(null);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithNullState() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*state.*"));

    task2.setThawedTime(AbstractTaskTest.NANOS5);
    pojo.setState((String) null);
    task2.setState(null);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithNewerState() throws Exception {
    task2.setThawedTime(AbstractTaskTest.NANOS5);
    task2.setFailureReason(null);
    task2.setFailureMessage(null);
    pojo.setState("some new state");
    task2.setState(State.UNKNOWN);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);

    Assert.assertThat(task.equals0(task2), Matchers.equalTo(true));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(true));
  }

  @Test
  public void testReadFromCurrentVersionWithNullDuration() throws Exception {
    exception.expect(TestInvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*duration.*"));

    pojo.setDuration(null);
    task2.setDuration(null);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);
  }

  @Test
  public void testReadFromCurrentVersionWithNewerStateDurations() throws Exception {
    pojo.setDuration("some new state", AbstractTaskTest.DURATION4);
    task2.setThawedTime(AbstractTaskTest.NANOS5);
    task2.setFailureReason(null);
    task2.setFailureMessage(null);
    task2.setDurations(
        Map.of(
            State.PENDING,
            AbstractTaskTest.DURATION2,
            State.PROCESSING,
            AbstractTaskTest.DURATION3,
            State.UNKNOWN,
            AbstractTaskTest.DURATION4));

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);

    Assert.assertThat(task.equals0(task2), Matchers.equalTo(true));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(true));
  }

  @Test
  public void testReadFromCurrentVersionWithWhenStateIsTransitional() throws Exception {
    task2.setThawedTime(AbstractTaskTest.NANOS5);
    task2.setFailureReason(null);
    task2.setFailureMessage(null);
    pojo.setFrozenTime(AbstractTaskTest.THEN4.toEpochMilli());
    pojo.setState(State.PENDING);
    task2.setState(State.PENDING);
    task2.setDuration(AbstractTaskTest.DURATION4.plus(AbstractTaskTest.DURATION5));
    task2.setDurations(
        Map.of(
            State.PENDING,
            AbstractTaskTest.DURATION2.plus(AbstractTaskTest.DURATION5),
            State.PROCESSING,
            AbstractTaskTest.DURATION3));

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);

    Assert.assertThat(task.equals0(task2), Matchers.equalTo(true));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(false));
  }

  @Test
  public void testReadFromUnknownPojo() throws Exception {
    task2.setThawedTime(AbstractTaskTest.NANOS5);
    task2.setFailureReason(null);
    task2.setFailureMessage(null);

    final TestAbstractTaskPojo pojo =
        new TestAbstractTaskPojo.Unknown()
            .setBaseVersion(AbstractTaskPojo.CURRENT_BASE_VERSION)
            .setVersion(TestAbstractTask.CURRENT_VERSION)
            .setId(AbstractTaskTest.ID)
            .setPriority(AbstractTaskTest.PRIORITY)
            .setOriginalQueuedTime(AbstractTaskTest.NOW)
            .setAttempts(AbstractTaskTest.ATTEMPTS)
            .setQueuedTime(AbstractTaskTest.THEN2)
            .setState(AbstractTaskTest.STATE)
            .setDuration(AbstractTaskTest.DURATION4)
            .setDuration(State.PENDING, AbstractTaskTest.DURATION2)
            .setDuration(State.PROCESSING, AbstractTaskTest.DURATION3)
            .setFrozenTime(AbstractTaskTest.NANOS);

    final TestAbstractTask task =
        new TestAbstractTask(
            AbstractTaskTest.mockClock(AbstractTaskTest.THEN5, AbstractTaskTest.NANOS5));

    task.readFrom(pojo);

    Assert.assertThat(task.equals0(task2), Matchers.equalTo(true));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(true));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    Assert.assertThat(AbstractTaskTest.TASK.hashCode0(), Matchers.equalTo(task2.hashCode0()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    task2.setPriority((byte) (AbstractTaskTest.PRIORITY + 1));

    Assert.assertThat(
        AbstractTaskTest.TASK.hashCode0(), Matchers.not(Matchers.equalTo(task2.hashCode0())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    Assert.assertThat(AbstractTaskTest.TASK.equals0(task2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(AbstractTaskTest.TASK.equals0(AbstractTaskTest.TASK), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(AbstractTaskTest.TASK.equals0(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with
                                               something else than expected */)
  @Test
  public void testEqualsWhenNotTheSameClass() throws Exception {
    Assert.assertThat(AbstractTaskTest.TASK.equals0("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    task2.setId(UUID.randomUUID());

    Assert.assertThat(AbstractTaskTest.TASK.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenOriginalQueuedTimeIsDifferent() throws Exception {
    task2.setOriginalQueuedTime(AbstractTaskTest.NOW.plusMillis(2L));

    Assert.assertThat(AbstractTaskTest.TASK.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenQueuedTimeIsDifferent() throws Exception {
    task2.setQueuedTime(AbstractTaskTest.NOW.plusMillis(2L));

    Assert.assertThat(AbstractTaskTest.TASK.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenPriorityIsDifferent() throws Exception {
    task2.setPriority((byte) (AbstractTaskTest.PRIORITY + 1));

    Assert.assertThat(AbstractTaskTest.TASK.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenAttemptsIsDifferent() throws Exception {
    task2.setAttempts(AbstractTaskTest.ATTEMPTS + 2);

    Assert.assertThat(AbstractTaskTest.TASK.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenThawedTimeIsDifferent() throws Exception {
    task2.setThawedTime(AbstractTaskTest.NANOS + 2L);

    Assert.assertThat(AbstractTaskTest.TASK.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenStateIsDifferent() throws Exception {
    task2.setState(State.UNKNOWN);

    Assert.assertThat(AbstractTaskTest.TASK.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenFailureReasonIsDifferent() throws Exception {
    task2.setFailureReason(TestErrorCode.UNKNOWN);

    Assert.assertThat(AbstractTaskTest.TASK.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenFailureMessageIsDifferent() throws Exception {
    task2.setFailureMessage(AbstractTaskTest.FAILURE_MSG + "2");

    Assert.assertThat(AbstractTaskTest.TASK.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDurationIsDifferent() throws Exception {
    task2.setDuration(AbstractTaskTest.DURATION4.plusMillis(2L));

    Assert.assertThat(AbstractTaskTest.TASK.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDurationsIsDifferent() throws Exception {
    task2.setDurations(Map.of(State.PENDING, AbstractTaskTest.DURATION2.plusMillis(2L)));

    Assert.assertThat(AbstractTaskTest.TASK.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  private static Clock mockClock(Instant wallTime, long monotonicTime, long... monotonicTimes) {
    return AbstractTaskTest.mockClock(wallTime, (Instant[]) null, monotonicTime, monotonicTimes);
  }

  private static Clock mockClock(
      Instant wallTime, Instant wallTime2, long monotonicTime, long... monotonicTimes) {
    return AbstractTaskTest.mockClock(
        wallTime, new Instant[] {wallTime2}, monotonicTime, monotonicTimes);
  }

  private static Clock mockClock(
      Instant wallTime,
      @Nullable Instant[] wallTimes,
      long monotonicTime,
      @Nullable long... monotonicTimes) {
    final Clock clock = Mockito.mock(Clock.class);

    if (ArrayUtils.isNotEmpty(wallTimes)) {
      Mockito.when(clock.wallTime())
          .thenReturn(
              wallTime.toEpochMilli(),
              Stream.of(wallTimes).map(Instant::toEpochMilli).toArray(Long[]::new))
          // poor's man way of achieving verifies without over-complicating the test method
          .thenThrow(new AssertionError("unexpected call to clock.wallTime()"));
    } else {
      Mockito.when(clock.wallTime())
          .thenReturn(wallTime.toEpochMilli())
          // poor's man way of achieving verifies without over-complicating the test method
          .thenThrow(new AssertionError("unexpected call to clock.wallTime()"));
    }
    if (ArrayUtils.isNotEmpty(monotonicTimes)) {
      Mockito.when(clock.monotonicTime())
          .thenReturn(monotonicTime, ArrayUtils.toObject(monotonicTimes))
          // poor's man way of achieving verifies without over-complicating the test method
          .thenThrow(new AssertionError("unexpected call to clock.monotonicTime()"));
    } else {
      Mockito.when(clock.monotonicTime())
          .thenReturn(monotonicTime)
          // poor's man way of achieving verifies without over-complicating the test method
          .thenThrow(new AssertionError("unexpected call to clock.monotonicTime()"));
    }
    return clock;
  }
}

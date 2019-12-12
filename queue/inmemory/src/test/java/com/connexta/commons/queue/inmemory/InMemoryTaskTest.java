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
package com.connexta.commons.queue.inmemory;

import com.connexta.commons.queue.Queue.Id;
import com.connexta.commons.queue.Task.State;
import com.connexta.commons.test.micrometer.MeterRegistryMock;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import io.micrometer.core.instrument.Tag;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class InMemoryTaskTest {
  private static final Id ID = Id.of(TestInMemoryTask.class, "1");
  private static final int CAPACITY = 2;
  private static final String PREFIX = "prefix";
  private static final Tag TAG = Mockito.mock(Tag.class);
  private static final Tag TAG2 = Mockito.mock(Tag.class);
  private static final byte PRIORITY = 3;
  private static final Duration DURATION2 = Duration.ofSeconds(5L);
  private static final Duration DURATION3 = Duration.ofSeconds(8L);
  private static final Duration DURATION4 = Duration.ofSeconds(17L);
  private static final Duration DURATION5 = Duration.ofSeconds(24L);
  private static final long NANOS = System.nanoTime();
  private static final long NANOS2 = InMemoryTaskTest.NANOS + InMemoryTaskTest.DURATION2.toNanos();
  private static final long NANOS3 = InMemoryTaskTest.NANOS2 + InMemoryTaskTest.DURATION3.toNanos();
  private static final long NANOS4 = InMemoryTaskTest.NANOS3 + InMemoryTaskTest.DURATION4.toNanos();
  private static final long NANOS5 = InMemoryTaskTest.NANOS4 + InMemoryTaskTest.DURATION5.toNanos();
  // using System.currentTimeMillis() ensures the same resolution when testing
  // as how the implementation currently creates Instant objects from a Clock
  private static final Instant NOW = Instant.ofEpochMilli(System.currentTimeMillis());
  private static final Instant THEN2 = InMemoryTaskTest.NOW.plusMillis(NANOS2);
  private static final Instant THEN3 = InMemoryTaskTest.NOW.plusMillis(NANOS3);
  private static final Instant THEN4 = InMemoryTaskTest.NOW.plusMillis(NANOS4);
  private static final Instant THEN5 = InMemoryTaskTest.NOW.plusMillis(NANOS5);
  private static final String REASON = "SomeIntelligentReason";

  private final TestTaskInfo info = new TestTaskInfo(InMemoryTaskTest.PRIORITY);

  private final MeterRegistryMock registry = new MeterRegistryMock();
  private final InMemoryQueueBroker broker =
      new TestInMemoryQueueBroker(
          InMemoryTaskTest.CAPACITY,
          InMemoryTaskTest.PREFIX,
          Stream.of(InMemoryTaskTest.TAG, InMemoryTaskTest.TAG2),
          registry);

  private final TestInMemoryOfferingQueue queue =
      Mockito.spy(
          new TestInMemoryOfferingQueue(broker, InMemoryTaskTest.ID) {
            @Override
            void requeue(
                InMemoryTask<TestException, TestErrorCode, TestTaskInfo, TestAbstractTaskPojo> task,
                boolean atFront,
                Runnable whenQueued) {
              whenQueued.run();
            }

            @Override
            void remove(
                InMemoryTask<TestException, TestErrorCode, TestTaskInfo, TestAbstractTaskPojo> task,
                Runnable whenRemoved)
                throws InterruptedException {
              whenRemoved.run();
            }
          });

  @Test
  public void testConstructorWhenInitiallyQueued() throws Exception {
    registry.mockClock(
        InMemoryTaskTest.NOW, // creation
        InMemoryTaskTest.NANOS, // creation
        InMemoryTaskTest.NANOS2, // when getDuration() is called
        InMemoryTaskTest.NANOS2, // when getDuration(State.PENDING) is called
        InMemoryTaskTest.NANOS2); // when getDuration(State.PROCESSING) is called
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    Assert.assertThat(task.getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(task.getPriority(), Matchers.equalTo(InMemoryTaskTest.PRIORITY));
    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(false));
    Assert.assertThat(task.isSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.isFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getFailureReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(task.getDuration(), Matchers.equalTo(InMemoryTaskTest.DURATION2));
    Assert.assertThat(
        task.getDuration(State.PENDING), Matchers.equalTo(InMemoryTaskTest.DURATION2));
    Assert.assertThat(task.getDuration(State.PROCESSING), Matchers.equalTo(Duration.ZERO));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());
  }

  @Test
  public void testLockOnFirstAttempt() throws Exception {
    registry.mockClock(
        InMemoryTaskTest.NOW, // creation
        InMemoryTaskTest.NANOS, // creation
        InMemoryTaskTest.NANOS2, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS3, // when getDuration() is called
        InMemoryTaskTest.NANOS3, // when getDuration(State.PENDING) is called
        InMemoryTaskTest.NANOS3); // when getDuration(State.PROCESSING) is called
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PROCESSING));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(false));
    Assert.assertThat(task.isSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.isFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getFailureReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(InMemoryTaskTest.DURATION2.plus(InMemoryTaskTest.DURATION3)));
    Assert.assertThat(
        task.getDuration(State.PENDING), Matchers.equalTo(InMemoryTaskTest.DURATION2));
    Assert.assertThat(
        task.getDuration(State.PROCESSING), Matchers.equalTo(InMemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(true));
    Assert.assertThat(
        task.getOwner(),
        OptionalMatchers.isPresentAnd(Matchers.sameInstance(Thread.currentThread())));
  }

  @Test
  public void testUnlockOnFirstAttempt() throws Exception {
    registry.mockClock(
        InMemoryTaskTest.NOW, // creation
        InMemoryTaskTest.THEN3, // when requeued
        InMemoryTaskTest.NANOS, // creation
        InMemoryTaskTest.NANOS2, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS3, // when unlock() is called and task is requeued to become pending
        InMemoryTaskTest.NANOS4, // when getDuration() is called
        InMemoryTaskTest.NANOS4, // when getDuration(State.PENDING) is called
        InMemoryTaskTest.NANOS4); // when getDuration(State.PROCESSING) is called
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.unlock();

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(false));
    Assert.assertThat(task.isSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.isFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getFailureReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(
            InMemoryTaskTest.DURATION2
                .plus(InMemoryTaskTest.DURATION3)
                .plus(InMemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getDuration(State.PENDING),
        Matchers.equalTo(InMemoryTaskTest.DURATION2.plus(InMemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getDuration(State.PROCESSING), Matchers.equalTo(InMemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue).requeue(Mockito.eq(task), Mockito.eq(true), Mockito.notNull());
    Mockito.verify(queue, Mockito.never()).remove(Mockito.any(), Mockito.any());
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void testUnlockWhenNotOwner() throws Exception {
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    lockFromOtherThread(task);

    task.unlock();
  }

  @Test(expected = IllegalStateException.class)
  public void testUnlockWhenAlreadyCompleted() throws Exception {
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.complete();

    task.unlock();
  }

  @Test(expected = IllegalStateException.class)
  public void testUnlockWhenAlreadyFailed() throws Exception {
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.fail(TestErrorCode.UNKNOWN);

    task.unlock();
  }

  @Test
  public void testCompleteOnFirstAttempt() throws Exception {
    registry.mockClock(
        InMemoryTaskTest.NOW, // creation
        InMemoryTaskTest.NANOS, // creation
        InMemoryTaskTest.NANOS2, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS3); // when complete() is called and task is removed
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.complete();

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.SUCCESSFUL));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(true));
    Assert.assertThat(task.isSuccessful(), Matchers.equalTo(true));
    Assert.assertThat(task.isFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getFailureReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(InMemoryTaskTest.DURATION2.plus(InMemoryTaskTest.DURATION3)));
    Assert.assertThat(
        task.getDuration(State.PENDING), Matchers.equalTo(InMemoryTaskTest.DURATION2));
    Assert.assertThat(
        task.getDuration(State.PROCESSING), Matchers.equalTo(InMemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue, Mockito.never())
        .requeue(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
    Mockito.verify(queue).remove(Mockito.eq(task), Mockito.any());
  }

  @Test
  public void testCompleteAfterFirstUnlocked() throws Exception {
    registry.mockClock(
        InMemoryTaskTest.NOW, // creation
        InMemoryTaskTest.NANOS, // creation
        InMemoryTaskTest.NANOS2, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS3, // when unlock() is called and task is requeued to become pending
        InMemoryTaskTest.NANOS4, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS5); // when complete() is called and task is removed
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.unlock();
    task.lock();
    task.complete();

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.SUCCESSFUL));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(true));
    Assert.assertThat(task.isSuccessful(), Matchers.equalTo(true));
    Assert.assertThat(task.isFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getFailureReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(
            InMemoryTaskTest.DURATION2
                .plus(InMemoryTaskTest.DURATION3)
                .plus(InMemoryTaskTest.DURATION4)
                .plus(InMemoryTaskTest.DURATION5)));
    Assert.assertThat(
        task.getDuration(State.PENDING),
        Matchers.equalTo(InMemoryTaskTest.DURATION2.plus(InMemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getDuration(State.PROCESSING),
        Matchers.equalTo(InMemoryTaskTest.DURATION3.plus(InMemoryTaskTest.DURATION5)));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue).requeue(Mockito.any(), Mockito.eq(true), Mockito.any());
    Mockito.verify(queue).remove(Mockito.eq(task), Mockito.any());
  }

  @Test
  public void testCompleteAfterSecondAttempt() throws Exception {
    registry.mockClock(
        InMemoryTaskTest.NOW, // creation
        InMemoryTaskTest.THEN3, // after requeued
        InMemoryTaskTest.NANOS, // creation
        InMemoryTaskTest.NANOS2, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS3, // when fail() is called and task is requeued to become pending
        InMemoryTaskTest.NANOS4, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS5); // when complete() is called and task is removed
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.fail(TestErrorCode.RETRYABLE_FAILURE);
    task.lock();
    task.complete();

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(2));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.SUCCESSFUL));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(true));
    Assert.assertThat(task.isSuccessful(), Matchers.equalTo(true));
    Assert.assertThat(task.isFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getFailureReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(InMemoryTaskTest.THEN3));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(
            InMemoryTaskTest.DURATION2
                .plus(InMemoryTaskTest.DURATION3)
                .plus(InMemoryTaskTest.DURATION4)
                .plus(InMemoryTaskTest.DURATION5)));
    Assert.assertThat(
        task.getDuration(State.PENDING),
        Matchers.equalTo(InMemoryTaskTest.DURATION2.plus(InMemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getDuration(State.PROCESSING),
        Matchers.equalTo(InMemoryTaskTest.DURATION3.plus(InMemoryTaskTest.DURATION5)));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue).requeue(Mockito.any(), Mockito.eq(false), Mockito.any());
    Mockito.verify(queue).remove(Mockito.eq(task), Mockito.any());
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void testCompleteWhenNotOwner() throws Exception {
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    lockFromOtherThread(task);

    task.complete();
  }

  @Test(expected = IllegalStateException.class)
  public void testCompleteWhenAlreadyCompleted() throws Exception {
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.complete();

    task.complete();
  }

  @Test(expected = IllegalStateException.class)
  public void testCompleteWhenAlreadyFailed() throws Exception {
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.fail(TestErrorCode.UNKNOWN);

    task.complete();
  }

  @Test
  public void testFailWhenRetryableAndWithNoReasonOnFirstAttempt() throws Exception {
    registry.mockClock(
        InMemoryTaskTest.NOW, // creation
        InMemoryTaskTest.THEN3, // when requeued
        InMemoryTaskTest.NANOS, // creation
        InMemoryTaskTest.NANOS2, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS3, // when fail() is called and task is requeued to become pending
        InMemoryTaskTest.NANOS4, // when getDuration() is called
        InMemoryTaskTest.NANOS4, // when getDuration(State.PENDING) is called
        InMemoryTaskTest.NANOS4); // when getDuration(State.PROCESSING) is called
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.fail(TestErrorCode.RETRYABLE_FAILURE);

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(2));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(false));
    Assert.assertThat(task.isSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.isFailed(), Matchers.equalTo(false));
    Assert.assertThat(
        task.getFailureReason(), OptionalMatchers.isPresentAndIs(TestErrorCode.RETRYABLE_FAILURE));
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(InMemoryTaskTest.THEN3));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(
            InMemoryTaskTest.DURATION2
                .plus(InMemoryTaskTest.DURATION3)
                .plus(InMemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getDuration(State.PENDING),
        Matchers.equalTo(InMemoryTaskTest.DURATION2.plus(InMemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getDuration(State.PROCESSING), Matchers.equalTo(InMemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue).requeue(Mockito.eq(task), Mockito.eq(false), Mockito.notNull());
    Mockito.verify(queue, Mockito.never()).remove(Mockito.any(), Mockito.any());
  }

  @Test
  public void testFailWhenRetryableAndWithReasonOnFirstAttempt() throws Exception {
    registry.mockClock(
        InMemoryTaskTest.NOW, // creation
        InMemoryTaskTest.THEN3, // when requeued
        InMemoryTaskTest.NANOS, // creation
        InMemoryTaskTest.NANOS2, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS3, // when fail() is called and task is requeued to become pending
        InMemoryTaskTest.NANOS4, // when getDuration() is called
        InMemoryTaskTest.NANOS4, // when getDuration(State.PENDING) is called
        InMemoryTaskTest.NANOS4); // when getDuration(State.PROCESSING) is called
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.fail(TestErrorCode.RETRYABLE_FAILURE, InMemoryTaskTest.REASON);

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(2));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(false));
    Assert.assertThat(task.isSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.isFailed(), Matchers.equalTo(false));
    Assert.assertThat(
        task.getFailureReason(), OptionalMatchers.isPresentAndIs(TestErrorCode.RETRYABLE_FAILURE));
    Assert.assertThat(
        task.getFailureMessage(), OptionalMatchers.isPresentAndIs(InMemoryTaskTest.REASON));
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(InMemoryTaskTest.THEN3));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(
            InMemoryTaskTest.DURATION2
                .plus(InMemoryTaskTest.DURATION3)
                .plus(InMemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getDuration(State.PENDING),
        Matchers.equalTo(InMemoryTaskTest.DURATION2.plus(InMemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getDuration(State.PROCESSING), Matchers.equalTo(InMemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue).requeue(Mockito.eq(task), Mockito.eq(false), Mockito.notNull());
    Mockito.verify(queue, Mockito.never()).remove(Mockito.any(), Mockito.any());
  }

  @Test
  public void testFailWhenNotRetryableAndWithNoReasonOnFirstAttempt() throws Exception {
    registry.mockClock(
        InMemoryTaskTest.NOW, // creation
        InMemoryTaskTest.NANOS, // creation
        InMemoryTaskTest.NANOS2, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS3); // when fail() is called and task is removed
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.fail(TestErrorCode.UNKNOWN);

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.FAILED));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(true));
    Assert.assertThat(task.isSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.isFailed(), Matchers.equalTo(true));
    Assert.assertThat(
        task.getFailureReason(), OptionalMatchers.isPresentAndIs(TestErrorCode.UNKNOWN));
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(InMemoryTaskTest.DURATION2.plus(InMemoryTaskTest.DURATION3)));
    Assert.assertThat(
        task.getDuration(State.PENDING), Matchers.equalTo(InMemoryTaskTest.DURATION2));
    Assert.assertThat(
        task.getDuration(State.PROCESSING), Matchers.equalTo(InMemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue, Mockito.never())
        .requeue(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
    Mockito.verify(queue).remove(Mockito.eq(task), Mockito.any());
  }

  @Test
  public void testFailWhenNotRetryableAndWithReasonOnFirstAttempt() throws Exception {
    registry.mockClock(
        InMemoryTaskTest.NOW, // creation
        InMemoryTaskTest.NANOS, // creation
        InMemoryTaskTest.NANOS2, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS3); // when fail() is called and task is removed
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.fail(TestErrorCode.UNKNOWN, InMemoryTaskTest.REASON);

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.FAILED));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(true));
    Assert.assertThat(task.isSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.isFailed(), Matchers.equalTo(true));
    Assert.assertThat(
        task.getFailureReason(), OptionalMatchers.isPresentAndIs(TestErrorCode.UNKNOWN));
    Assert.assertThat(
        task.getFailureMessage(), OptionalMatchers.isPresentAndIs(InMemoryTaskTest.REASON));
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(InMemoryTaskTest.DURATION2.plus(InMemoryTaskTest.DURATION3)));
    Assert.assertThat(
        task.getDuration(State.PENDING), Matchers.equalTo(InMemoryTaskTest.DURATION2));
    Assert.assertThat(
        task.getDuration(State.PROCESSING), Matchers.equalTo(InMemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue, Mockito.never())
        .requeue(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
    Mockito.verify(queue).remove(Mockito.eq(task), Mockito.any());
  }

  @Test
  public void testFailWhenNotRetryableAndAfterSecondAttempt() throws Exception {
    registry.mockClock(
        InMemoryTaskTest.NOW, // creation
        InMemoryTaskTest.THEN3, // when requeued
        InMemoryTaskTest.NANOS, // creation
        InMemoryTaskTest.NANOS2, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS3, // when fail() is called and task is requeued to become pending
        InMemoryTaskTest.NANOS4, // when lock() is called and task becomes active
        InMemoryTaskTest.NANOS5); // when fail() is called and task is removed
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.fail(TestErrorCode.RETRYABLE_FAILURE, InMemoryTaskTest.REASON);
    task.lock();
    task.fail(TestErrorCode.UNKNOWN);

    Assert.assertThat(task.getAttempts(), Matchers.equalTo(2));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.FAILED));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(true));
    Assert.assertThat(task.isSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.isFailed(), Matchers.equalTo(true));
    Assert.assertThat(
        task.getFailureReason(), OptionalMatchers.isPresentAndIs(TestErrorCode.UNKNOWN));
    Assert.assertThat(task.getFailureMessage(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(InMemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(InMemoryTaskTest.THEN3));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(
            InMemoryTaskTest.DURATION2
                .plus(InMemoryTaskTest.DURATION3)
                .plus(InMemoryTaskTest.DURATION4)
                .plus(InMemoryTaskTest.DURATION5)));
    Assert.assertThat(
        task.getDuration(State.PENDING),
        Matchers.equalTo(InMemoryTaskTest.DURATION2.plus(InMemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getDuration(State.PROCESSING),
        Matchers.equalTo(InMemoryTaskTest.DURATION3.plus(InMemoryTaskTest.DURATION5)));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue).requeue(Mockito.any(), Mockito.eq(false), Mockito.any());
    Mockito.verify(queue).remove(Mockito.eq(task), Mockito.any());
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void testFailWhenNotOwner() throws Exception {
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    lockFromOtherThread(task);

    task.fail(TestErrorCode.UNKNOWN);
  }

  @Test(expected = IllegalStateException.class)
  public void testFailWhenAlreadyCompleted() throws Exception {
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.complete();

    task.fail(TestErrorCode.UNKNOWN);
  }

  @Test(expected = IllegalStateException.class)
  public void testFailWhenAlreadyFailed() throws Exception {
    final TestInMemoryTask task = new TestInMemoryTask(queue, info);

    task.lock();
    task.fail(TestErrorCode.UNKNOWN);

    task.fail(TestErrorCode.UNKNOWN);
  }

  private void lockFromOtherThread(TestInMemoryTask task) throws InterruptedException {
    final Thread other = new Thread(task::lock);

    other.setDaemon(true);
    other.start();
    other.join();

    Assert.assertThat(task.getOwner(), OptionalMatchers.isPresentAndIs(other));
  }
}

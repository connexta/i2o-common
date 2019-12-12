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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class InMemoryCompositeQueueTest {
  private static final int QUEUE_CAPACITY = 5;
  private static final String PREFIX = "prefix";
  private static final Tag TAG = Mockito.mock(Tag.class);
  private static final Tag TAG2 = Mockito.mock(Tag.class);
  private static final Id ID = Id.of(TestInMemoryTask.class, "1");
  private static final Id ID2 = Id.of(TestInMemoryTask.class, "2");
  private static final Id ID3 = Id.of(TestInMemoryTask.class, "3");
  private static final Id ID4 = Id.of(TestInMemoryTask.class, "4");
  private static final int SIZE = 4;
  private static final int PENDING_SIZE = 3;
  private static final int PROCESSING_SIZE = 1;
  private static final int REMAINING_CAPACITY = 3;
  private static final int SIZE2 = 0;
  private static final int PENDING_SIZE2 = 0;
  private static final int PROCESSING_SIZE2 = 0;
  private static final int REMAINING_CAPACITY2 = 8;
  private static final int SIZE3 = 14;
  private static final int PENDING_SIZE3 = 8;
  private static final int PROCESSING_SIZE3 = 6;
  private static final int REMAINING_CAPACITY3 = 5;

  private final TestInMemoryQueueBroker broker =
      new TestInMemoryQueueBroker(
          InMemoryCompositeQueueTest.QUEUE_CAPACITY,
          InMemoryCompositeQueueTest.PREFIX,
          Stream.of(InMemoryCompositeQueueTest.TAG, InMemoryCompositeQueueTest.TAG2),
          new MeterRegistryMock());
  private final TestInMemoryOfferingQueue queue =
      mock(
          InMemoryCompositeQueueTest.ID,
          InMemoryCompositeQueueTest.SIZE,
          InMemoryCompositeQueueTest.PENDING_SIZE,
          InMemoryCompositeQueueTest.PROCESSING_SIZE,
          InMemoryCompositeQueueTest.REMAINING_CAPACITY);
  private final TestInMemoryOfferingQueue queue2 =
      mock(
          InMemoryCompositeQueueTest.ID2,
          InMemoryCompositeQueueTest.SIZE2,
          InMemoryCompositeQueueTest.PENDING_SIZE2,
          InMemoryCompositeQueueTest.PROCESSING_SIZE2,
          InMemoryCompositeQueueTest.REMAINING_CAPACITY2);
  private final TestInMemoryOfferingQueue queue3 =
      mock(
          InMemoryCompositeQueueTest.ID3,
          InMemoryCompositeQueueTest.SIZE3,
          InMemoryCompositeQueueTest.PENDING_SIZE3,
          InMemoryCompositeQueueTest.PROCESSING_SIZE3,
          InMemoryCompositeQueueTest.REMAINING_CAPACITY3);

  private final TestInMemoryCompositeQueue composite =
      new TestInMemoryCompositeQueue(
          broker,
          Stream.of(
              InMemoryCompositeQueueTest.ID,
              InMemoryCompositeQueueTest.ID3,
              InMemoryCompositeQueueTest.ID4));

  private TestInMemoryOfferingQueue mock(
      Id id, int size, int pendingSize, int processingSize, int remainingCapacity) {
    final TestInMemoryOfferingQueue queue = Mockito.spy(new TestInMemoryOfferingQueue(broker, id));

    Mockito.doReturn(size).when(queue).size();
    Mockito.doReturn(pendingSize).when(queue).size(State.PENDING);
    Mockito.doReturn(processingSize).when(queue).size(State.PROCESSING);
    Mockito.doReturn(remainingCapacity).when(queue).remainingCapacity();
    broker.add(queue);
    return queue;
  }

  @Test
  public void testConstructor() throws Exception {
    Assert.assertThat(
        composite.ids().collect(Collectors.toList()),
        Matchers.containsInAnyOrder(
            InMemoryCompositeQueueTest.ID,
            InMemoryCompositeQueueTest.ID3,
            InMemoryCompositeQueueTest.ID4));
  }

  @Test
  public void testQueues() throws Exception {
    Assert.assertThat(
        composite.queues().collect(Collectors.toList()),
        Matchers.containsInAnyOrder(Matchers.sameInstance(queue), Matchers.sameInstance(queue3)));
  }

  @Test
  public void testGetQueueWhenQueueDefined() throws Exception {
    Assert.assertThat(
        composite.getQueue(InMemoryCompositeQueueTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.sameInstance(queue)));
  }

  @Test
  public void testGetQueueWhenQueueNotDefined() throws Exception {
    Assert.assertThat(
        composite.getQueue(InMemoryCompositeQueueTest.ID4), OptionalMatchers.isEmpty());
  }

  @Test
  public void testGetQueueWhenQueueIdWasNotDefined() throws Exception {
    Assert.assertThat(
        composite.getQueue(InMemoryCompositeQueueTest.ID2), OptionalMatchers.isEmpty());
  }

  @Test
  public void testSize() throws Exception {
    Assert.assertThat(
        composite.size(),
        Matchers.equalTo(InMemoryCompositeQueueTest.SIZE + InMemoryCompositeQueueTest.SIZE3));

    Mockito.verify(queue).size();
    Mockito.verify(queue2, Mockito.never()).size();
    Mockito.verify(queue3).size();
  }

  @Test
  public void testSizeForPendingState() throws Exception {
    Assert.assertThat(
        composite.size(State.PENDING),
        Matchers.equalTo(
            InMemoryCompositeQueueTest.PENDING_SIZE + InMemoryCompositeQueueTest.PENDING_SIZE3));

    Mockito.verify(queue).size(State.PENDING);
    Mockito.verify(queue2, Mockito.never()).size(Mockito.any());
    Mockito.verify(queue3).size(State.PENDING);
  }

  @Test
  public void testSizeForProcessingState() throws Exception {
    Assert.assertThat(
        composite.size(State.PROCESSING),
        Matchers.equalTo(
            InMemoryCompositeQueueTest.PROCESSING_SIZE
                + InMemoryCompositeQueueTest.PROCESSING_SIZE3));

    Mockito.verify(queue).size(State.PROCESSING);
    Mockito.verify(queue2, Mockito.never()).size(Mockito.any());
    Mockito.verify(queue3).size(State.PROCESSING);
  }

  @Test
  public void testRemainingCapacity() throws Exception {
    Assert.assertThat(
        composite.remainingCapacity(),
        Matchers.equalTo(
            InMemoryCompositeQueueTest.REMAINING_CAPACITY
                + InMemoryCompositeQueueTest.REMAINING_CAPACITY3));

    Mockito.verify(queue).remainingCapacity();
    Mockito.verify(queue2, Mockito.never()).remainingCapacity();
    Mockito.verify(queue3).remainingCapacity();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testTakeIsNotSupported() throws Exception {
    composite.take();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPollIsNotSupported() throws Exception {
    composite.poll(1L, TimeUnit.HOURS);
  }

  @Test
  public void testClear() throws Exception {
    Mockito.doNothing().when(queue).clear();
    Mockito.doNothing().when(queue3).clear();

    composite.clear();

    Mockito.verify(queue).clear();
    Mockito.verify(queue2, Mockito.never()).clear();
    Mockito.verify(queue3).clear();
  }
}

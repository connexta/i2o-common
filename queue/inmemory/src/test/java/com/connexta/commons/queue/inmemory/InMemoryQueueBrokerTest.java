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
import com.connexta.commons.test.micrometer.MeterRegistryMock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class InMemoryQueueBrokerTest {
  private static final Id ID = Id.of(TestInMemoryTask.class, "1");
  private static final Id ID2 = Id.of(TestInMemoryTask.class, "2");
  private static final int CAPACITY = 2;
  private static final String PREFIX = "prefix";
  private static final Tag TAG = Mockito.mock(Tag.class);
  private static final Tag TAG2 = Mockito.mock(Tag.class);
  private static final MeterRegistry REGISTRY = new MeterRegistryMock();

  private final TestInMemoryQueueBroker broker =
      new TestInMemoryQueueBroker(
          InMemoryQueueBrokerTest.CAPACITY,
          InMemoryQueueBrokerTest.PREFIX,
          Stream.of(InMemoryQueueBrokerTest.TAG, InMemoryQueueBrokerTest.TAG2),
          InMemoryQueueBrokerTest.REGISTRY);

  @Test
  public void testConstructor() throws Exception {
    Assert.assertThat(
        broker.getQueueCapacity(), Matchers.equalTo(InMemoryQueueBrokerTest.CAPACITY));
    Assert.assertThat(broker.getMetricPrefix(), Matchers.equalTo(InMemoryQueueBrokerTest.PREFIX));
    Assert.assertThat(
        broker.metricTags().collect(Collectors.toList()),
        Matchers.containsInAnyOrder(InMemoryQueueBrokerTest.TAG, InMemoryQueueBrokerTest.TAG2));
    Assert.assertThat(
        broker.getMeterRegistry(), Matchers.sameInstance(InMemoryQueueBrokerTest.REGISTRY));
    Assert.assertThat(broker.size(), Matchers.equalTo(0));
  }

  @Test
  public void testConstructorWithPrefixEndingWithADot() throws Exception {
    final TestInMemoryQueueBroker broker =
        new TestInMemoryQueueBroker(
            InMemoryQueueBrokerTest.CAPACITY,
            InMemoryQueueBrokerTest.PREFIX + ".",
            Stream.of(InMemoryQueueBrokerTest.TAG, InMemoryQueueBrokerTest.TAG2),
            InMemoryQueueBrokerTest.REGISTRY);

    Assert.assertThat(broker.getMetricPrefix(), Matchers.equalTo(InMemoryQueueBrokerTest.PREFIX));
  }

  @Test
  public void testGetQueue() throws Exception {
    final TestInMemoryOfferingQueue queue = broker.getQueue(InMemoryQueueBrokerTest.ID);
  }
}

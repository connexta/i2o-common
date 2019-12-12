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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class QueueBrokerTest {
  private static final Id ID = Id.of(Task.class, "id");
  private static final Id ID2 = Id.of(Task.class, "id2");
  private static final Id ID3 = Id.of(Task.class, "id3");

  private final QueueBroker broker = Mockito.mock(QueueBroker.class, Mockito.CALLS_REAL_METHODS);

  @Test
  public void testGetQueueByIdsWithMultipleIds() throws Exception {
    final CompositeQueue queue = Mockito.mock(CompositeQueue.class);

    Mockito.when(broker.getQueue(Mockito.any(Stream.class))).thenReturn(queue);

    Assert.assertThat(
        broker.getQueue(QueueBrokerTest.ID, QueueBrokerTest.ID2, QueueBrokerTest.ID3),
        Matchers.sameInstance(queue));

    final ArgumentCaptor<Stream<Id>> ids = ArgumentCaptor.forClass(Stream.class);

    Mockito.verify(broker, Mockito.never()).getQueue(Mockito.any(Id.class));
    Mockito.verify(broker).getQueue(ids.capture());

    Assert.assertThat(
        ids.getValue().collect(Collectors.toList()),
        Matchers.containsInRelativeOrder(
            QueueBrokerTest.ID, QueueBrokerTest.ID2, QueueBrokerTest.ID3));
  }

  @Test
  public void testGetQueueByIdsWithASingleId() throws Exception {
    final OfferingQueue queue = Mockito.mock(OfferingQueue.class);

    Mockito.when(broker.getQueue(Mockito.any(Id.class))).thenReturn(queue);

    Assert.assertThat(broker.getQueue(new Id[] {QueueBrokerTest.ID}), Matchers.sameInstance(queue));

    Mockito.verify(broker).getQueue(QueueBrokerTest.ID);
    Mockito.verify(broker, Mockito.never()).getQueue(Mockito.any(Stream.class));
  }
}

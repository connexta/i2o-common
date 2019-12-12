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
import com.github.npathai.hamcrestopt.OptionalMatchers;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class CompositeQueueTest {
  private static final Id ID = Id.of(Task.class, "1");
  private static final Id ID2 = Id.of(Task.class, "2");
  private static final Id ID3 = Id.of(Task.class, "3");

  private static final OfferingQueue<?, ?, ?, ?> QUEUE = Mockito.mock(OfferingQueue.class);
  private static final OfferingQueue<?, ?, ?, ?> QUEUE2 = Mockito.mock(OfferingQueue.class);
  private static final OfferingQueue<?, ?, ?, ?> QUEUE3 = Mockito.mock(OfferingQueue.class);

  static {
    Mockito.when(CompositeQueueTest.QUEUE.getId()).thenReturn(CompositeQueueTest.ID);
    Mockito.when(CompositeQueueTest.QUEUE2.getId()).thenReturn(CompositeQueueTest.ID2);
    Mockito.when(CompositeQueueTest.QUEUE3.getId()).thenReturn(CompositeQueueTest.ID3);
  }

  private final CompositeQueue<?, ?, ?, ?, ?> queue =
      Mockito.mock(CompositeQueue.class, Mockito.CALLS_REAL_METHODS);

  @Test
  public void testIds() throws Exception {
    Mockito.when(queue.queues())
        .thenAnswer(
            (Answer)
                i ->
                    Stream.of(
                        CompositeQueueTest.QUEUE,
                        CompositeQueueTest.QUEUE2,
                        CompositeQueueTest.QUEUE3));

    Assert.assertThat(
        queue.ids().collect(Collectors.toList()),
        Matchers.containsInAnyOrder(
            CompositeQueueTest.ID, CompositeQueueTest.ID2, CompositeQueueTest.ID3));
  }

  @Test
  public void testGetQueueWhenPresent() throws Exception {
    Mockito.when(queue.queues())
        .thenAnswer(
            (Answer)
                i ->
                    Stream.of(
                        CompositeQueueTest.QUEUE,
                        CompositeQueueTest.QUEUE2,
                        CompositeQueueTest.QUEUE3));

    Assert.assertThat(
        queue.getQueue(CompositeQueueTest.ID2),
        OptionalMatchers.isPresentAnd(Matchers.sameInstance(CompositeQueueTest.QUEUE2)));
  }
}

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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.stream.Stream;

public class TestInMemoryQueueBroker
    extends InMemoryQueueBroker<
        TestException,
        TestErrorCode,
        TestTaskInfo,
        TestAbstractTaskPojo,
        TestInMemoryOfferingQueue,
        TestInMemoryQueue,
        TestInMemoryCompositeQueue,
        TestInMemoryTask> {
  public TestInMemoryQueueBroker(
      int queueCapacity, String prefix, Stream<Tag> tags, MeterRegistry meterRegistry) {
    super(
        TestException.class,
        TestInvalidFieldException::new,
        TestUnsupportedVersionException::new,
        queueCapacity,
        prefix,
        tags,
        meterRegistry);
  }

  @Override
  protected TestInMemoryOfferingQueue createQueue(Id id) {
    return new TestInMemoryOfferingQueue(this, id);
  }

  @Override
  protected TestInMemoryCompositeQueue createQueue(Stream<Id> ids) {
    return new TestInMemoryCompositeQueue(this, ids);
  }
}

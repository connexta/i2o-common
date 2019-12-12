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

public class TestInMemoryOfferingQueue
    extends InMemoryOfferingQueue<
        TestException, TestErrorCode, TestTaskInfo, TestAbstractTaskPojo, TestInMemoryTask>
    implements TestInMemoryQueue {
  public TestInMemoryOfferingQueue(InMemoryQueueBroker broker, Id id) {
    super(broker, id);
  }

  @Override
  protected TestInMemoryTask createTask(TestTaskInfo info) {
    return new TestInMemoryTask(this, info);
  }
}

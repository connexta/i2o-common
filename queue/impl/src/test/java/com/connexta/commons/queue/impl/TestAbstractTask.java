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

import com.connexta.commons.queue.impl.persistence.TestAbstractTaskPojo;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class TestAbstractTask
    extends AbstractTask<TestException, TestErrorCode, TestTaskInfo, TestAbstractTaskPojo> {
  public static final String PERSISTABLE_TYPE = "test task";

  public static final int CURRENT_VERSION = 3;

  private TestTaskInfo info;

  public TestAbstractTask(TestTaskInfo info, Clock clock) {
    super(
        TestAbstractTask.PERSISTABLE_TYPE,
        TestException.class,
        TestInvalidFieldException::new,
        TestUnsupportedVersionException::new,
        info.getPriority(),
        clock);
    this.info = info;
  }

  public TestAbstractTask(TestAbstractTaskPojo pojo, Clock clock) throws TestException {
    super(
        TestAbstractTask.PERSISTABLE_TYPE,
        TestException.class,
        TestInvalidFieldException::new,
        TestUnsupportedVersionException::new,
        clock);
    readFrom(pojo);
  }

  public TestAbstractTask(Clock clock) throws TestException {
    super(
        TestAbstractTask.PERSISTABLE_TYPE,
        TestException.class,
        TestInvalidFieldException::new,
        TestUnsupportedVersionException::new,
        clock);
  }

  @Override
  public TestTaskInfo getInfo() {
    return info;
  }

  @Override
  public boolean isLocked() {
    return false;
  }

  @Override
  public void unlock() throws InterruptedException, TestException {}

  @Override
  public void complete() throws InterruptedException, TestException {}

  @Override
  public void fail(TestErrorCode code) throws InterruptedException, TestException {}

  @Override
  public void fail(TestErrorCode code, String reason) throws InterruptedException, TestException {}

  @Override
  protected TestAbstractTaskPojo writeTo(TestAbstractTaskPojo pojo) throws TestException {
    super.writeTo(pojo);

    return pojo.setVersion(TestAbstractTask.CURRENT_VERSION);
  }

  @Override
  protected void readFrom(TestAbstractTaskPojo pojo) throws TestException {
    super.readFrom(pojo);
  }

  @Override
  protected void setId(UUID id) {
    super.setId(id);
  }

  @Override
  void setPriority(byte priority) {
    super.setPriority(priority);
  }

  @Override
  void setOriginalQueuedTime(Instant originalQueuedTime) {
    super.setOriginalQueuedTime(originalQueuedTime);
  }

  @Override
  void setAttempts(int attempts) {
    super.setAttempts(attempts);
  }

  @Override
  void setQueuedTime(Instant queueTime) {
    super.setQueuedTime(queueTime);
  }

  @Override
  void setState(State state) {
    super.setState(state);
  }

  @Override
  void setThawedTime(long thawedTime) {
    super.setThawedTime(thawedTime);
  }

  @Override
  void setDuration(Duration duration) {
    super.setDuration(duration);
  }

  @Override
  void setDurations(Map<State, Duration> durations) {
    super.setDurations(durations);
  }

  void setInfo(TestTaskInfo info) {
    this.info = info;
  }
}

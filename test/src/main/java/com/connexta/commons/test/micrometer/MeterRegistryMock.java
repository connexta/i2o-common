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
package com.connexta.commons.test.micrometer;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;
import io.micrometer.core.instrument.search.RequiredSearch;
import io.micrometer.core.instrument.search.Search;
import io.micrometer.core.lang.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.mockito.Mockito;

/** Provides a mocked version of micrometer's registry suitable for multi-threaded test cases. */
public class MeterRegistryMock extends MeterRegistry {
  private Clock clock = null;

  public MeterRegistryMock() {
    super(Clock.SYSTEM);
  }

  /**
   * Changes the clock that will be used by this meter registry.
   *
   * @param clock the clock to use and returned from the config
   */
  public void setClock(Clock clock) {
    this.clock = clock;
  }

  /**
   * Mocks the clock to return the specified wall and monotonic times in the provided sequence.
   *
   * <p><i>Note:</i> {@link AssertionError} will be generated if the clock is accessed more times
   * than specified.
   *
   * @param wallTime the wall time to return
   * @param monotonicTime the first monotonic time to return
   * @param monotonicTimes the monotonic times to return after the first one in the provided
   *     sequence
   */
  public void mockClock(Instant wallTime, long monotonicTime, long... monotonicTimes) {
    mockClock(wallTime, (Instant[]) null, monotonicTime, monotonicTimes);
  }

  /**
   * Mocks the clock to return the specified wall and monotonic times in the provided sequence.
   *
   * <p><i>Note:</i> {@link AssertionError} will be generated if the clock is accessed more times
   * than specified.
   *
   * @param wallTime the first wall time to return
   * @param wallTime2 the second wall time to return after the first one
   * @param monotonicTime the first monotonic time to return
   * @param monotonicTimes the monotonic times to return after the first one in the provided
   *     sequence
   */
  public void mockClock(
      Instant wallTime, Instant wallTime2, long monotonicTime, long... monotonicTimes) {
    mockClock(wallTime, new Instant[] {wallTime2}, monotonicTime, monotonicTimes);
  }

  /**
   * Mocks the clock to return the specified wall and monotonic times in the provided sequence.
   *
   * <p><i>Note:</i> {@link AssertionError} will be generated if the clock is accessed more times
   * than specified.
   *
   * @param wallTime the first wall time to return
   * @param wallTimes the wall time to return after the first one in the provided sequence
   * @param monotonicTime the first monotonic time to return
   * @param monotonicTimes the monotonic times to return after the first one in the provided
   *     sequence
   */
  public void mockClock(
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
    this.clock = clock;
  }

  @Override
  public List<Meter> getMeters() {
    return Collections.emptyList();
  }

  @Override
  public void forEachMeter(Consumer<? super Meter> consumer) {}

  @Override
  public MeterRegistry.Config config() {
    return new MeterRegistry.Config() {
      @Override
      public Config commonTags(Iterable<Tag> tags) {
        return this;
      }

      @Override
      public synchronized Config meterFilter(MeterFilter filter) {
        return this;
      }

      @Override
      public Config onMeterAdded(Consumer<Meter> meter) {
        return this;
      }

      @Override
      public Config onMeterRemoved(Consumer<Meter> meter) {
        return this;
      }

      @Override
      public Config namingConvention(NamingConvention convention) {
        return this;
      }

      @Override
      public Clock clock() {
        return (clock != null) ? clock : Clock.SYSTEM;
      }

      @Override
      public Config pauseDetector(PauseDetector detector) {
        return this;
      }

      @Override
      public PauseDetector pauseDetector() {
        return Mockito.mock(PauseDetector.class);
      }
    };
  }

  @Override
  public Search find(String name) {
    return Mockito.mock(Search.class);
  }

  @Override
  public RequiredSearch get(String name) {
    return Mockito.mock(RequiredSearch.class);
  }

  @Override
  public Counter counter(String name, Iterable<Tag> tags) {
    return Mockito.mock(Counter.class);
  }

  @Override
  public DistributionSummary summary(String name, Iterable<Tag> tags) {
    return Mockito.mock(DistributionSummary.class);
  }

  @Override
  public Timer timer(String name, Iterable<Tag> tags) {
    return Mockito.mock(Timer.class);
  }

  @Override
  public MeterRegistry.More more() {
    return Mockito.mock(MeterRegistry.More.class);
  }

  @Override
  @Nullable
  public <T> T gauge(
      String name, Iterable<Tag> tags, @Nullable T obj, ToDoubleFunction<T> valueFunction) {
    return obj;
  }

  @Override
  public <T extends Collection<?>> T gaugeCollectionSize(
      String name, Iterable<Tag> tags, T collection) {
    return collection;
  }

  @Override
  public <T extends Map<?, ?>> T gaugeMapSize(String name, Iterable<Tag> tags, T map) {
    return map;
  }

  @Override
  public Meter remove(Meter meter) {
    return null;
  }

  @Override
  public Meter remove(Id id) {
    return null;
  }

  @Override
  public void clear() {}

  @Override
  public void close() {}

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  protected <T> Gauge newGauge(Id id, T t, ToDoubleFunction<T> toDoubleFunction) {
    return Mockito.mock(Gauge.class);
  }

  @Override
  protected Counter newCounter(Id id) {
    return Mockito.mock(Counter.class);
  }

  @Override
  protected LongTaskTimer newLongTaskTimer(Id id) {
    return Mockito.mock(LongTaskTimer.class);
  }

  @Override
  protected Timer newTimer(
      Id id, DistributionStatisticConfig distributionStatisticConfig, PauseDetector pauseDetector) {
    return Mockito.mock(Timer.class);
  }

  @Override
  protected DistributionSummary newDistributionSummary(
      Id id, DistributionStatisticConfig distributionStatisticConfig, double v) {
    return Mockito.mock(DistributionSummary.class);
  }

  @Override
  protected Meter newMeter(Id id, Type type, Iterable<Measurement> iterable) {
    return Mockito.mock(Meter.class);
  }

  @Override
  protected <T> FunctionTimer newFunctionTimer(
      Id id,
      T t,
      ToLongFunction<T> toLongFunction,
      ToDoubleFunction<T> toDoubleFunction,
      TimeUnit timeUnit) {
    return Mockito.mock(FunctionTimer.class);
  }

  @Override
  protected <T> FunctionCounter newFunctionCounter(
      Id id, T t, ToDoubleFunction<T> toDoubleFunction) {
    return Mockito.mock(FunctionCounter.class);
  }

  @Override
  protected TimeUnit getBaseTimeUnit() {
    return TimeUnit.MILLISECONDS;
  }

  @Override
  protected DistributionStatisticConfig defaultHistogramConfig() {
    return Mockito.mock(DistributionStatisticConfig.class);
  }
}

/**
 * Copyright (c) 2013, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.cloudera.gertrude;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * A mapping between experiment flag names and the rules for calculating the value of a flag.
 *
 * <p>Clients should usually not interact with this class directly, but rather access its
 * contents on a per-request basis via the {@link ExperimentState#get} method.
 */
public final class ExperimentFlagSettings {

  private static final Logger log = LoggerFactory.getLogger(ExperimentFlagSettings.class);

  private final ExperimentFlagSettings delegate;
  private final Map<String, ? extends FlagValueCalculator<Object>> entries;

  ExperimentFlagSettings() {
    this(ImmutableMap.<String, FlagValueCalculator<Object>>of());
  }

  ExperimentFlagSettings(Map<String, ? extends FlagValueCalculator<Object>> entries) {
    this(null, entries);
  }

  ExperimentFlagSettings(
      ExperimentFlagSettings delegate,
      Map<String, ? extends FlagValueCalculator<Object>> entries) {
    this.delegate = delegate;
    this.entries = Preconditions.checkNotNull(entries);
  }

  <T> FlagValue<T> getValue(ExperimentFlag<T> flag, ExperimentState state) {
    FlagValueCalculator<T> calc = (FlagValueCalculator<T>) entries.get(flag.getName());
    if (calc == null) {
      if (delegate == null) {
        log.warn("No calculator defined for experiment flag: {}", flag);
        return FlagValue.of(flag.getDefaultValue(), Condition.CacheLevel.RELOAD);
      } else {
        return delegate.getValue(flag, state);
      }
    } else {
      return calc.apply(state);
    }
  }

  ExperimentFlagSettings withOverrides(Map<String, ? extends FlagValueCalculator<Object>> overrides) {
    if (overrides.isEmpty()) {
      return this;
    }
    return new ExperimentFlagSettings(this, overrides);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ExperimentFlagSettings that = (ExperimentFlagSettings) o;

    return (delegate == null ? that.delegate == null : delegate.equals(that.delegate)) &&
        entries.equals(that.entries);
  }

  @Override
  public int hashCode() {
    int result = delegate != null ? delegate.hashCode() : 0;
    result = 31 * result + entries.hashCode();
    return result;
  }
}

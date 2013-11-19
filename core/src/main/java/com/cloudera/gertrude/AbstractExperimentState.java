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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * Abstract base class that implements the core functionality of the {@link ExperimentState} interface.
 * Clients should create their own {@code ExperimentState} implementations that use this instance as a base
 * class, since there are core methods in the framework (such as {@link ExperimentHandler#handle(AbstractExperimentState)}
 * that expect to operate on a subclass of {@code AbstractExperimentState}. More details on the purpose of this
 * split are described in the documentation of the {@link ExperimentState} interface.
 */
public abstract class AbstractExperimentState implements ExperimentState {

  private final Map<ExperimentFlag<?>, Object> valueCache = Maps.newHashMap();
  private final Set<Integer> experimentIds = Sets.newHashSet();
  private final long requestTimeMsec = System.currentTimeMillis();

  private ExperimentFlagSettings flagSettings;

  @Override
  public abstract Optional<String> getDiversionIdentifier(int diversionId);

  @Override
  public Set<Integer> forceExperimentIds() {
    return ImmutableSet.of();
  }

  @Override
  public <T> T get(ExperimentFlag<T> flag) {
    if (valueCache.containsKey(flag)) {
      return (T) valueCache.get(flag);
    } else if (flagSettings != null) {
      FlagValue<T> value = flagSettings.getValue(flag, this);
      if (value.getCacheLevel() != Condition.CacheLevel.NONE) {
        valueCache.put(flag, value.getValue());
      }
      return value.getValue();
    } else {
      return flag.getDefaultValue();
    }
  }

  @Override
  public int getInt(ExperimentFlag<Long> longFlag) {
    return get(longFlag).intValue();
  }

  @Override
  public float getFloat(ExperimentFlag<Double> doubleFlag) {
    return get(doubleFlag).floatValue();
  }

  @Override
  public Set<Integer> getExperimentIds()  {
    return ImmutableSet.copyOf(experimentIds);
  }

  @Override
  public boolean isDiverted() {
    return flagSettings != null;
  }

  @Override
  public long getRequestTimeMsec() {
    return requestTimeMsec;
  }

  void setFlagSettings(ExperimentFlagSettings flagSettings) {
    this.valueCache.clear();
    this.flagSettings = flagSettings;
  }

  void addExperimentId(int experimentId) {
    this.experimentIds.add(experimentId);
  }
}

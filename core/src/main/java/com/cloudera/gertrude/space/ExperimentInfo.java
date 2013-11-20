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
package com.cloudera.gertrude.space;

import com.cloudera.gertrude.DiversionCriterion;
import com.cloudera.gertrude.ExperimentState;
import com.cloudera.gertrude.FlagValueCalculator;
import com.cloudera.gertrude.Segment;
import com.cloudera.gertrude.calculate.FlagValueCalculatorImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Information about a particular experiment.
 */
public final class ExperimentInfo implements Segment {

  private final SegmentInfo info;
  private final Map<String, FlagValueCalculatorImpl<Object>> baseOverrides;
  private final Map<Integer, Map<String, FlagValueCalculatorImpl<Object>>> launchOverrides;
  private boolean disabled;

  public ExperimentInfo(
      SegmentInfo info,
      Map<String, FlagValueCalculatorImpl<Object>> baseOverrides,
      Map<Integer, Map<String, FlagValueCalculatorImpl<Object>>> launchOverrides) {
    this.info = info;
    this.baseOverrides = baseOverrides;
    this.launchOverrides = launchOverrides;
    this.disabled = false;
  }

  @Override
  public int getId() {
    return info.getId();
  }

  @Override
  public int getLayerId() {
    return info.getLayerId();
  }

  @Override
  public SortedSet<Integer> getBuckets() {
    return info.getBuckets();
  }

  @Override
  public long getStartTimeMsec() {
    return info.getStartTimeMsec();
  }

  @Override
  public long getEndTimeMsec() {
    return info.getEndTimeMsec();
  }

  @Override
  public boolean isEnabled(long requestTimeMsec) {
    return !disabled && getStartTimeMsec() < requestTimeMsec && requestTimeMsec < getEndTimeMsec();
  }

  @Override
  public void disable() {
    this.disabled = true;
  }


  @Override
  public boolean isValidFor(ExperimentState state) {
    return info.isValidFor(state);
  }

  @Override
  public void handle(
      ExperimentState state,
      List<DiversionCriterion> diversionCriteria,
      Map<String, FlagValueCalculator<Object>> overrides,
      Set<Integer> newExperimentIds) {
    newExperimentIds.add(getId());
    if (info.overridesEnabled(state.getRequestTimeMsec())) { // check pre-period/post-period
      overrides.putAll(getOverrides(newExperimentIds));
    }
  }


  Map<String, ? extends FlagValueCalculator<Object>> getOverrides(Set<Integer> newExperimentIds) {
    Set<Integer> interactions = Sets.intersection(newExperimentIds, launchOverrides.keySet());
    if (interactions.isEmpty()) {
      return baseOverrides;
    } else {
      Map<String, FlagValueCalculatorImpl<Object>> ret = Maps.newHashMap(baseOverrides);
      for (Integer launchLayerExperimentId : interactions) {
        ret.putAll(launchOverrides.get(launchLayerExperimentId));
      }
      return ret;
    }
  }
}

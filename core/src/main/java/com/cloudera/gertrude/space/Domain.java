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
import com.cloudera.gertrude.Layer;
import com.cloudera.gertrude.Segment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public final class Domain implements Segment {
  private final SegmentInfo info;
  private final Set<Layer> layers;

  public Domain(SegmentInfo info, Set<Layer> layers) {
    this.info = info;
    this.layers = layers;
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
  public boolean isValidFor(ExperimentState state) {
    return info.isValidFor(state);
  }

  @Override
  public void handle(
      ExperimentState state,
      List<DiversionCriterion> diversionCriteria,
      Map<String, FlagValueCalculator<Object>> overrides,
      Set<Integer> newExperimentIds) {
    for (Layer layer : layers) {
      layer.assign(state, diversionCriteria, overrides, newExperimentIds);
    }
  }
}

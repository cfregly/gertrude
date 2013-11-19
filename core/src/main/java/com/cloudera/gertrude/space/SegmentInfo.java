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

import com.cloudera.gertrude.Condition;
import com.cloudera.gertrude.ExperimentState;

import java.util.SortedSet;

public final class SegmentInfo {
  private final int id;
  private final int layerId;
  private final int diversionId;
  private final SortedSet<Integer> buckets;
  private final Condition<ExperimentState> condition;
  private final long startTimeMsec;
  private final long endTimeMsec;
  private final long prePeriodTimeMsec;
  private final long postPeriodTimeMsec;

  public SegmentInfo(int id, int layerId, int diversionId, SortedSet<Integer> buckets) {
    this(id, layerId, diversionId, buckets, Condition.TRUE, 0, Long.MAX_VALUE, 0, Long.MAX_VALUE);
  }

  public SegmentInfo(int id,
                     int layerId,
                     int diversionId,
                     SortedSet<Integer> buckets,
                     Condition<ExperimentState> condition,
                     long startTimeMsec,
                     long endTimeMsec,
                     long prePeriodTimeMsec,
                     long postPeriodTimeMsec) {
    this.id = id;
    this.layerId = layerId;
    this.diversionId = diversionId;
    this.buckets = buckets;
    this.condition = condition;
    this.startTimeMsec = startTimeMsec;
    this.endTimeMsec = endTimeMsec;
    this.prePeriodTimeMsec = prePeriodTimeMsec;
    this.postPeriodTimeMsec = postPeriodTimeMsec;
  }

  public int getId() {
    return id;
  }

  public SortedSet<Integer> getBuckets() {
    return buckets;
  }

  public int getLayerId() {
    return layerId;
  }

  public int getDiversionId() {
    return diversionId;
  }

  public long getStartTimeMsec() {
    return startTimeMsec;
  }

  public long getEndTimeMsec() {
    return endTimeMsec;
  }

  public long getPrePeriodTimeMsec() {
    return prePeriodTimeMsec;
  }

  public long getPostPeriodTimeMsec() {
    return postPeriodTimeMsec;
  }

  public boolean overridesEnabled(long timeMsec) {
    return prePeriodTimeMsec <= timeMsec && timeMsec < postPeriodTimeMsec;
  }

  public boolean isActive(long timeMsec) {
    return startTimeMsec <= timeMsec && timeMsec < endTimeMsec;
  }

  // Tests whether or not the experiment state applies to the conditions associated
  // with this segment
  public boolean isValidFor(ExperimentState state) {
    return condition.evaluate(state);
  }
}

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
  private final Condition condition;

  public SegmentInfo(int id, int layerId, int diversionId, SortedSet<Integer> buckets) {
    this(id, layerId, diversionId, buckets, Condition.TRUE);
  }

  public SegmentInfo(int id, int layerId, int diversionId, SortedSet<Integer> buckets, Condition condition) {
    this.id = id;
    this.layerId = layerId;
    this.diversionId = diversionId;
    this.buckets = buckets;
    this.condition = condition;
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

  // Tests whether or not the experiment state applies to the conditions associated
  // with this segment
  public boolean isValidFor(ExperimentState state) {
    return condition.evaluate(state);
  }
}

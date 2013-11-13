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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * A vertical slice of buckets contained inside of a {@link Layer} that has associated {@link Condition} logic
 * for diversion and overrides to the default flag values.
 *
 * <p>There are two implementations of this interface. An <b>experiment</b> is a specific configuration of
 * conditions and flag overrides that describes a hypothesis that we are interested in testing. A <b>domain</b>
 * is a higher-level segment that can contain other {@code Layer} instances (with their own {@code Segment}s)
 * nested inside of it. Domains are a useful way of creating shared space for experiments that need to cut
 * across multiple layers (or even binaries) in order to be valid. They can also be used to create custom
 * partitions of the experiment space for long-running experiments or ones where there are concerns about
 * lingering after effects of the experiment on subjects.
 */
public interface Segment {

  /**
   * The unique id of this {@code Segment} instance in the current {@link ExperimentSpace}.
   *
   * @return the unique id of this {@code Segment}
   */
  int getId();

  /**
   * The unique id of the {@link Layer} that owns this {@code Segment}.
   *
   * @return the layer id
   */
  int getLayerId();

  /**
   * The set of buckets claimed by this {@code Segment} within its assigned {@link Layer} and
   * {@link DiversionCriterion}.
   */
  SortedSet<Integer> getBuckets();

  /**
   * Returns true if the given {@code ExperimentState} satisfies the {@link Condition} instances associated with
   * this {@code Segment}.
   *
   * @param state the current {@code ExperimentState}
   * @return true if the conditions evaluate to true, false otherwise
   */
  boolean isValidFor(ExperimentState state);

  /**
   * Modify the given {@code overrides} and update {@code newExperimentIds} with the ids of any experiments
   * that this request was diverted into.
   *
   * @param state the current {@code ExperimentState}
   * @param diversionCriteria the active diversion criteria
   * @param overrides the current overrides to flag value calculations
   * @param newExperimentIds the experiment ids that have been added on this diversion request
   */
  void handle(ExperimentState state,
              List<DiversionCriterion> diversionCriteria,
              Map<String, FlagValueCalculator<Object>> overrides,
              Set<Integer> newExperimentIds);
}

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

/**
 * A horizontal slice of {@link ExperimentSpace} that contains subclasses of the {@link Segment} interface, which
 * represent experiments and domains.
 *
 * <p>An {@link ExperimentState} will be diverted into at most one {@code Segment} inside of each {@code Layer}
 * instance, depending on the attributes of the {@code ExperimentState} and the active diversion criteria. Layers
 * are designed so that diversion across layers is independent; knowing which experiment a request was diverted into
 * in one layer does not provide any information about what requests it will divert into in a different layer.
 */
public interface Layer {

  /**
   * Assign the given state to a {@code Segment} of this {@code Layer} based on the given {@code diversionCriteria}
   * and update the {@code overrides} and set of {@code newExperimentIds} based on the settings in that {@code Segment}.
   *
   * @param state the {@code ExperimentState} undergoing diversion
   * @param diversionCriteria the configured {@link DiversionCriterion} applied to the state
   * @param overrides the current set of overrides to the default {@code FlagValueCalculator} instances
   * @param newExperimentIds the list of new experiment ids this state will be added to
   */
  void assign(
      ExperimentState state,
      List<DiversionCriterion> diversionCriteria,
      Map<String, FlagValueCalculator<Object>> overrides,
      Set<Integer> newExperimentIds);

  /**
   * Returns true if this is a launch layer instance, whose experiments have priority over other layers for overriding
   * flag value calculations.
   *
   * @return true if this is a launch layer
   */
  boolean isLaunchLayer();
}

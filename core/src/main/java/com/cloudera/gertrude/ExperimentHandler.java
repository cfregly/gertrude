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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.Set;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Manages experiment diversion for an {@link ExperimentState} based on the data contained in the current
 * {@link ExperimentSpace}.
 *
 * <p>The handler is the link between an {@code ExperimentState} and the currently configured {@link ExperimentSpace}.
 * As new requests are passed to the {@link #handle(AbstractExperimentState)} method, the {@code ExperimentHandler}
 * diverts them into experiments that modifies their flag value calculators that are used for the rest of the request
 * by calling the {@link ExperimentState#get(ExperimentFlag)} methods:
 *
 * <pre>   {@code
 * static ExperimentFlag<Boolean> AWESOME_FEATURE_ON = Experiments.declare("awesome", false);
 * public void doStuff(Request request) {
 *    MyExperimentState state = new MyExperimentState(request);
 *    Experiments.getHandler().handle(state);
 *    if (state.get(AWESOME_FEATURE_ON)) {
 *      // enable *AWESOME* feature
 *    } else {
 *      // enable feature suggested by product manager
 *    }
 * }}</pre>
 *
 * <p>New {@code ExperimentSpace} configurations may be updated asynchronously by the {@link ExperimentSpaceLoader}
 * that is configured for use with this {@code ExperimentHandler} in the {@link Experiments} namespace. Subsequent
 * calls to the {@link #handle(AbstractExperimentState)} method will use the latest updates to the
 * {@code ExperimentSpace} for processing requests, and the {@code ExperimentHandler} is thread-safe.
 */
public final class ExperimentHandler {

  private final MetricRegistry metrics;
  private final Meter requests;

  private volatile ExperimentSpace experimentSpace = new ExperimentSpace();

  ExperimentHandler(MetricRegistry metrics) {
    this.metrics = Preconditions.checkNotNull(metrics);
    this.requests = this.metrics.meter(name(ExperimentHandler.class, "requests"));
  }

  /**
   * Diverts the given {@code ExperimentState} into one or more experiments across the
   * {@link Layer} instances in the current {@link ExperimentSpace}.
   *
   * <p>A given {@code ExperimentState} can only be diverted into one experiment per layer;
   * attempting to re-divert an instance that has already been diverted has no effect on
   * the flag values or experiment ids associated with the state.
   *
   * @param state the request to divert
   */
  public void handle(AbstractExperimentState state) {
    requests.mark();

    Set<Integer> newExperimentIds = Sets.newHashSet();
    experimentSpace.diversion(state, newExperimentIds);

    if (newExperimentIds.isEmpty()) {
      metrics.meter(name(ExperimentHandler.class, "nodiversion")).mark();
    } else {
      for (Integer id : newExperimentIds) {
        metrics.meter(name(ExperimentHandler.class, String.valueOf(id))).mark();
        state.addExperimentId(id);
      }
    }
  }

  /**
   * Disables the experiment with the given id, preventing it from diverting any traffic.
   *
   * @param experimentId id of the experiment to disable
   * @return true if the experiment was found and disabled, false otherwise
   */
  public boolean disable(int experimentId) {
    return experimentSpace.disable(experimentId);
  }

  /**
   * Returns the version string for the {@code ExperimentSpace} that is currently being used
   * by this instance to handle requests.
   *
   * @return the version string for the current {@code ExperimentSpace}
   */
  public String getVersionIdentifier() {
    return experimentSpace.getVersionIdentifier();
  }

  void update(ExperimentSpace experimentSpace) {
    this.experimentSpace = experimentSpace;
  }
}

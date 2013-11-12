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
package com.cloudera.gertrude.server;

import com.cloudera.gertrude.ExperimentFlag;
import com.cloudera.gertrude.ExperimentState;

import javax.servlet.ServletRequest;

/**
 * Some convenience methods for working with the {@link ExperimentState} in client code.
 */
public final class GertrudeUtils {
  static final String GERTRUDE_EXPERIMENT_STATE_KEY = "GERTRUDE_EXPERIMENT_STATE";

  /**
   * Retrieves a {@code HttpServletExperimentState} from a {@code ServletRequest}.
   *
   * @param request the request
   * @return a {@code HttpServletExperimentState} associated with the request, or null if none exists
   */
  public static ExperimentState getState(ServletRequest request) {
    return (ExperimentState) request.getAttribute(GERTRUDE_EXPERIMENT_STATE_KEY);
  }

  /**
   * Shortcut for getting the value of an {@link ExperimentFlag} from a {@code ServletRequest} that
   * contains an {@code ExperimentState}.
   *
   * @param request the current request
   * @param flag the value whose value for the current request is returned
   * @return the value of the flag, using the state associated with the request, or the default value of the flag
   */
  public static <T> T getFlagValue(ServletRequest request, ExperimentFlag<T> flag) {
    ExperimentState state = getState(request);
    return state == null ? flag.getDefaultValue() : state.get(flag);
  }

  private GertrudeUtils() {}
}

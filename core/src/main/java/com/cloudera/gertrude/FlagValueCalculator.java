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

/**
 * Logic for calculating the value of a named experiment flag for a given {@link ExperimentState}.
 *
 * <p>Clients do not typically work with this interface directly, but rather access it through the
 * {@link ExperimentState#get} method for retrieving and caching the values of {@link ExperimentFlag}
 * instances.
 */
public interface FlagValueCalculator<T> {

  /**
   * Compute the value of this flag for the given {@code ExperimentState}.
   *
   * @param state the current state
   * @return the {@link FlagValue} containing the calculated value and its {@code ConditionLevel}.
   */
  FlagValue<T> apply(ExperimentState state);
}

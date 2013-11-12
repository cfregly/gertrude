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
package com.cloudera.gertrude.condition;

import com.cloudera.gertrude.Condition;
import com.cloudera.gertrude.ExperimentState;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

/**
 * A base class for the common {@link Condition} pattern of checking whether or not the value of a field
 * in an {@link ExperimentState} belongs to a given set of values.
 *
 * <p>For example, the property might be the country that a request originated in, and the arguments to
 * the function would be a list of country codes. If the country returned by the {@code #getValue} method
 * is in the given list of country codes, then the {@code #evaluate} method would return {@code true}.
 */
public abstract class AbstractPropertyCondition<V, S extends ExperimentState> implements Condition<S> {

  private Set<V> matchingValues = ImmutableSet.of();

  @Override
  public void initialize(List<String> args) {
    this.matchingValues = parseArgs(args);
  }

  @Override
  public boolean evaluate(S state) {
    return matchingValues.contains(getValue(state));
  }

  /**
   * Sub-classes should override this method to validate and extract the information they need to evalate
   * the state of a given {@code ExperimentState} instance.
   *
   * @param args the input arguments from the configuration
   * @return the set of values to match on
   */
  protected abstract Set<V> parseArgs(List<String> args);

  /**
   * Returns the value of the property for the given state.
   *
   * @param state the {@code ExperimentState} to evaluate
   * @return the value of the property checked by this condition
   */
  public abstract V getValue(S state);
}

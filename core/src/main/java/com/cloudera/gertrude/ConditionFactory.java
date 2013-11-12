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

import java.util.Set;

/**
 * Associates a set of names with implementations of the {@link Condition} interface and
 * creates new instances of those implementations on request from a client.
 *
 * <p>A {@code ConditionFactory} must be registered in the {@link Experiments} namespace before
 * the {@link ExperimentHandler} for this server can be used by clients.
 */
public interface ConditionFactory {

  /**
   * Returns the set of names that have been associated with subclasses of the {@code Condition}
   * interface in this instance.
   *
   * @return the set of known names that will return a valid result from the {@link #create} method
   */
  Set<String> supportedNames();

  /**
   * Creates a new {@code Condition} instance that is associated with the given name.
   *
   * @param name a short name used to describe the {@code Condition} instance to create
   * @return a new {@code Condition} instance, or null if the name is unknown
   */
  Condition<? extends ExperimentState> create(String name);
}

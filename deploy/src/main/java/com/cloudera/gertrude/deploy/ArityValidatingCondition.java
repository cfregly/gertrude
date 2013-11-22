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
package com.cloudera.gertrude.deploy;

import com.cloudera.gertrude.Condition;
import com.cloudera.gertrude.ExperimentState;

import java.util.List;

class ArityValidatingCondition implements Condition<ExperimentState> {

  private final String name;
  private final int minArgs;
  private final int maxArgs;

  ArityValidatingCondition(String name, int minArgs, int maxArgs) {
    this.name = name;
    this.minArgs = minArgs;
    this.maxArgs = maxArgs;
  }

  @Override
  public void initialize(List<String> args) {
    if (args.size() < minArgs || args.size() > maxArgs) {
      throw new IllegalArgumentException(String.format("Invalid arg size = %d for condition \"%s\" (min %d, max %d)",
          args.size(), name, minArgs, maxArgs));
    }
  }

  @Override
  public boolean evaluate(ExperimentState state) {
    return true;
  }

  @Override
  public CacheLevel getCacheLevel() {
    return CacheLevel.NONE;
  }
}

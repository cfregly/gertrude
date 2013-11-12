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
import com.cloudera.gertrude.ConditionFactory;
import com.cloudera.gertrude.ExperimentState;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CompositeConditionFactory implements ConditionFactory {
  private final Map<String, ConditionFactory> factories = Maps.newHashMap();

  public static CompositeConditionFactory create(ConditionFactory... factories) {
    return new CompositeConditionFactory(Arrays.<ConditionFactory>asList(factories));
  }

  public CompositeConditionFactory(List<ConditionFactory> factories) {
    for (ConditionFactory f : factories) {
      for (String name : f.supportedNames()) {
        if (this.factories.put(name, f) != null) {
          throw new IllegalStateException("Multiple condition factories support name: " + name);
        }
      }
    }
  }

  @Override
  public Set<String> supportedNames() {
    return factories.keySet();
  }

  @Override
  public Condition<? extends ExperimentState> create(String name) {
    return factories.get(name).create(name);
  }
}

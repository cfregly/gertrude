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

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;

public final class TestConditionFactory implements ConditionFactory {

  private final Map<String, Condition> conditions;

  public TestConditionFactory() {
    this.conditions = Maps.newHashMap();
  }

  public TestConditionFactory put(String name, Condition c) {
    this.conditions.put(name, c);
    return this;
  }

  @Override
  public Set<String> supportedNames() {
    return conditions.keySet();
  }

  @Override
  public Condition create(String name) {
    return conditions.get(name);
  }
}

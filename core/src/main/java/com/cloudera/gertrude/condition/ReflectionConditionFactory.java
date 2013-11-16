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

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

public final class ReflectionConditionFactory implements ConditionFactory {
  private final Map<String, Class<? extends Condition<ExperimentState>>> classes = Maps.newHashMap();

  public ReflectionConditionFactory register(String name, Class<? extends Condition<ExperimentState>> clazz) {
    if (classes.put(name, clazz) != null) {
      throw new IllegalStateException("Multiple condition classes for name: " + name);
    }
    return this;
  }


  @Override
  public Set<String> supportedNames() {
    return classes.keySet();
  }

  @Override
  public Condition<ExperimentState> create(String name) {
    try {
      return classes.get(name).getConstructor().newInstance();
    } catch (InstantiationException e) {
      throw new IllegalStateException("Could not create instance of condition function named: " + name, e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Could not create instance of condition function named: " + name, e);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Could not create instance of condition function named: " + name, e);
    } catch (InvocationTargetException e) {
      throw new IllegalStateException("Could not create instance of condition function named: " + name, e);
    }
  }
}

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
package com.cloudera.gertrude.calculate;

import com.cloudera.gertrude.Condition;
import com.cloudera.gertrude.ExperimentState;
import com.cloudera.gertrude.FlagValue;
import com.cloudera.gertrude.FlagValueCalculator;

import java.util.List;

public final class FlagValueCalculatorImpl<T> implements FlagValueCalculator<T> {
  private final T baseValue;
  private final List<Modifier<T>> modifiers;

  public FlagValueCalculatorImpl(T baseValue, List<Modifier<T>> modifiers) {
    this.baseValue = baseValue;
    this.modifiers = modifiers;
  }

  @Override
  public FlagValue<T> apply(ExperimentState state) {
    FlagValue<T> cv = FlagValue.of(baseValue, Condition.CacheLevel.RELOAD);
    for (Modifier<T> m : modifiers) {
      m.apply(cv, state);
    }
    return cv;
  }

  T getBaseValue() { return baseValue; }
  List<Modifier<T>> getModifiers() { return modifiers; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    FlagValueCalculatorImpl<T> that = (FlagValueCalculatorImpl<T>) o;

    if (baseValue != null ? !baseValue.equals(that.baseValue) : that.baseValue != null) {
      return false;
    }
    if (modifiers != null ? !modifiers.equals(that.modifiers) : that.modifiers != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = baseValue != null ? baseValue.hashCode() : 0;
    result = 31 * result + (modifiers != null ? modifiers.hashCode() : 0);
    return result;
  }
}

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
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;

public final class BasicModifier<T> implements Modifier<T> {

  private final T value;
  private final AssociativeOperator<? super T> operator;
  private final Condition<ExperimentState> condition;
  private final List<Modifier<T>> modifiers;

  public BasicModifier(T value, AssociativeOperator<? super T> operator) {
    this(value, operator, Condition.TRUE);
  }

  public BasicModifier(T value, AssociativeOperator<? super T> operator, Condition<ExperimentState> condition) {
    this(value, operator, condition, ImmutableList.<Modifier<T>>of());
  }

  public BasicModifier(
      T value,
      AssociativeOperator<? super T> operator,
      Condition<ExperimentState> condition,
      List<Modifier<T>> modifiers) {
    this.value = Preconditions.checkNotNull(value);
    this.operator = Preconditions.checkNotNull(operator);
    this.condition = Preconditions.checkNotNull(condition);
    this.modifiers = ImmutableList.copyOf(modifiers);
  }

  @Override
  public void apply(FlagValue<T> baseValue, ExperimentState state) {
    if (condition.evaluate(state)) {
      FlagValue<T> local = FlagValue.of(value, condition.getCacheLevel());
      for (Modifier<T> m : modifiers) {
        m.apply(local, state);
      }
      T newValue = (T) operator.apply(baseValue.getValue(), local.getValue());
      baseValue.update(newValue, local.getCacheLevel());
    } else {
      baseValue.updateCacheLevel(condition.getCacheLevel());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BasicModifier that = (BasicModifier) o;

    if (!condition.equals(that.condition)) return false;
    if (!modifiers.equals(that.modifiers)) return false;
    if (!operator.equals(that.operator)) return false;
    if (!value.equals(that.value)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = value.hashCode();
    result = 31 * result + operator.hashCode();
    result = 31 * result + condition.hashCode();
    result = 31 * result + modifiers.hashCode();
    return result;
  }
}

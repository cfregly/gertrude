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

import java.util.List;

public final class BooleanConditions {

  public static <S extends ExperimentState> Condition<S> and(List<Condition<S>> conditions) {
    return new AndCondition<S>(conditions);
  }

  public static <S extends ExperimentState> Condition<S> or(List<Condition<S>> conditions) {
    return new OrCondition<S>(conditions);
  }

  public static <S extends ExperimentState> Condition<S> not(Condition<S> condition) {
    return new NotCondition<S>(condition);
  }

  private BooleanConditions() {}

  private static final class AndCondition<S extends ExperimentState> implements Condition<S> {

    private final List<Condition<S>> conditions;
    private final CacheLevel cacheLevel;

    private AndCondition(List<Condition<S>> conditions) {
      this.conditions = conditions;
      CacheLevel cl = CacheLevel.RELOAD;
      for (Condition<?> f : conditions) {
        cl = cl.merge(f.getCacheLevel());
      }
      this.cacheLevel = cl;
    }

    @Override
    public void initialize(List<String> args) {
    }

    @Override
    public boolean evaluate(S state) {
      for (Condition<S> f : conditions) {
        if (!f.evaluate(state)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public CacheLevel getCacheLevel() {
      return cacheLevel;
    }
  }

  private static final class OrCondition<S extends ExperimentState> implements Condition<S> {

    private final List<Condition<S>> conditions;
    private final CacheLevel cacheLevel;

    private OrCondition(List<Condition<S>> functions) {
      this.conditions = functions;
      CacheLevel cl = CacheLevel.RELOAD;
      for (Condition<?> f : functions) {
        cl = cl.merge(f.getCacheLevel());
      }
      this.cacheLevel = cl;
    }

    @Override
    public void initialize(List<String> args) {
    }

    @Override
    public boolean evaluate(S state) {
      for (Condition<S> f : conditions) {
        if (f.evaluate(state)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public CacheLevel getCacheLevel() {
      return cacheLevel;
    }
  }

  private static final class NotCondition<S extends ExperimentState>  implements Condition<S> {

    private final Condition<S> delegate;

    private NotCondition(Condition<S> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void initialize(List<String> args) {
    }

    @Override
    public boolean evaluate(S state) {
      return !delegate.evaluate(state);
    }

    @Override
    public CacheLevel getCacheLevel() {
      return delegate.getCacheLevel();
    }
  }
}

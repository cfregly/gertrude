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

import java.util.List;

/**
 * Determines a true or false value for a given {@link ExperimentState}.
 *
 * <p>The {@code Condition} interface allows Gertrude clients to specify rules
 * that determine which experiments and flag value modifiers are active for a
 * request.
 *
 * <p>For example, a client could implement a {@code Condition} that tested
 * whether or not a request was in a particular country, or one based on a
 * regular expression matching the hostname of the machine running this instance.
 *
 * <p>Classes that implement the {@code Condition} interface should be registered
 * with the {@link ConditionFactory} for the current session so that Gertrude can
 * create new instances of those classes from the information contained in the
 * serialized version of the current {@link ExperimentSpace}.
 */
public interface Condition<S extends ExperimentState> {

  /**
   * Initialize this instance with an optional list of arguments provided in
   * the serialized {@code ExperimentSpace}.
   *
   * @param args the provided arguments
   */
  void initialize(List<String> args);

  /**
   * Returns a true or false value for the given {@code ExperimentState}.
   *
   * @param state the {@code ExperimentState} that contains information about the request
   * @return true or false, depending on the logic of this instance and the provided state
   */
  boolean evaluate(S state);

  /**
   * Returns an indication of the level at which this {@code Condition} instance may be
   * cached.
   * @return the cache level of this instance
   */
  CacheLevel getCacheLevel();

  /**
   * An indicator for how often the value of a {@code Condition} will change when the
   * {@link #evaluate(ExperimentState)} method is called repeatedly.
   */
  enum CacheLevel {

    /**
     * Indicates that the result of a call to {@link #evaluate(ExperimentState)} may never
     * be cached because the result is allowed to change during the lifetime of a single
     * request.
     */
    NONE {
      @Override
      public CacheLevel merge(CacheLevel other) {
        return CacheLevel.NONE;
      }
    },

    /**
     * Indicates that the result of a call to {@link #evaluate(ExperimentState)} will not change
     * when the same {@code ExperimentState} instance is passed to the {@code Condition} multiple
     * times.
     */
    REQUEST {
      @Override
      public CacheLevel merge(CacheLevel other) {
        if (other == NONE) {
          return NONE;
        } else {
          return CacheLevel.REQUEST;
        }
      }
    },

    /**
     * Indicates that the result of a call to {@link #evaluate(ExperimentState)} will not change
     * at all for the current {@code ExperimentSpace} configuration, independent of the value of
     * the {@code ExperimentState} argument.
     */
    RELOAD {
      @Override
      public CacheLevel merge(CacheLevel other) {
        return other;
      }
    };

    public abstract CacheLevel merge(CacheLevel other);
  }

  /**
   * A {@code Condition} instance that is always true.
   */
  Condition<ExperimentState> TRUE = new Condition<ExperimentState>() {
    @Override
    public void initialize(List<String> args) {
    }

    @Override
    public boolean evaluate(ExperimentState state) {
      return true;
    }

    @Override
    public CacheLevel getCacheLevel() {
      return CacheLevel.RELOAD;
    }
  };

  /**
   * A {@code Condition} instance that is always false.
   */
  Condition<ExperimentState> FALSE = new Condition<ExperimentState>() {
    @Override
    public void initialize(List<String> args) {
    }

    @Override
    public boolean evaluate(ExperimentState state) {
      return false;
    }

    @Override
    public CacheLevel getCacheLevel() {
      return CacheLevel.RELOAD;
    }
  };
}

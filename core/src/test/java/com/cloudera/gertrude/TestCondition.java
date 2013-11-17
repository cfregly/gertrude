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

public final class TestCondition<S extends ExperimentState> implements Condition<S> {

  private boolean result;
  private CacheLevel cacheLevel;

  public TestCondition() {
    this(false, CacheLevel.NONE);
  }

  public TestCondition(boolean result, CacheLevel cacheLevel) {
    this.result = result;
    this.cacheLevel = cacheLevel;
  }

  public TestCondition<?> setResult(boolean result) {
    this.result = result;
    return this;
  }

  public TestCondition<?> setCacheLevel(CacheLevel cacheLevel) {
    this.cacheLevel = cacheLevel;
    return this;
  }

  @Override
  public void initialize(List<String> args) {
  }

  @Override
  public boolean evaluate(S state) {
    return result;
  }

  @Override
  public CacheLevel getCacheLevel() {
    return cacheLevel;
  }
}

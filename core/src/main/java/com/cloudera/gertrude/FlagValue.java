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

/**
 * A utility class for tracking the value of a flag and its {@code CacheLevel} across a request.
 *
 * <p>Clients do not usually access instances of this class directly, but use them indirectly via
 * the {@link ExperimentState#get(ExperimentFlag)} method.
 *
 */
public final class FlagValue<T> {
  private T value;
  private Condition.CacheLevel cacheLevel;

  public static <T> FlagValue<T> of(T value, Condition.CacheLevel cacheLevel) {
    return new FlagValue<T>(value, cacheLevel);
  }

  public FlagValue(T value, Condition.CacheLevel cacheLevel) {
    this.value = value;
    this.cacheLevel = cacheLevel;
  }

  public T getValue() {
    return value;
  }

  public Condition.CacheLevel getCacheLevel() {
    return cacheLevel;
  }

  public void update(T newValue, Condition.CacheLevel newLevel) {
    this.value = newValue;
    updateCacheLevel(newLevel);
  }

  public void updateCacheLevel(Condition.CacheLevel newLevel) {
    this.cacheLevel = cacheLevel.merge(newLevel);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    FlagValue<T> that = (FlagValue<T>) o;

    return cacheLevel == that.cacheLevel &&
        (value == null ? that.value == null : value.equals(that.value));
  }

  @Override
  public int hashCode() {
    int result = value != null ? value.hashCode() : 0;
    result = 31 * result + (cacheLevel != null ? cacheLevel.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return new StringBuilder(value.toString()).append('(').append(cacheLevel).append(')').toString();
  }
}

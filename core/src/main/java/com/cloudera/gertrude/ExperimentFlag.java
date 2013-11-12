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

import com.google.common.base.Preconditions;

/**
 * A named, typed parameter whose value for an {@link ExperimentState} can change depending on
 * the experiments that the {@code ExperimentState} has been diverted into.
 *
 * <p>Instances of this class are created via the static factory methods defined in the
 * {@link Experiments} namespace:
 * <pre> {@code
 *
 *   ExperimentFlag<Double> foo = Experiments.declare("foo_param", 17.29);
 *   ExperimentFlag<Long> bar = Experiments.declare("bar_field", 13);
 *   ExperimentFlag<String> baz = Experiments.declare("baz", "");}</pre>
 * <p>The value of an {@code ExperimentFlag} for a request is retrieved from an
 * {@code ExperimentState}:
 * <pre> {@code
 *
 *   ExperimentState myState = ...;
 *   double fooValue = myState.get(foo);
 *   float fooFloat = myState.getFloat(foo);
 *   long barValue = myState.get(bar);
 *   int barInt = myState.getInt(bar);
 *   String bazStr = myState.get(baz);}</pre>
 */
public final class ExperimentFlag<T> {

  private final String name;
  private final FlagTypeParser<T> flagTypeParser;
  private final T defaultValue;

  ExperimentFlag(String name, FlagTypeParser<T> flagTypeParser, T defaultValue) {
    this.name = Preconditions.checkNotNull(name);
    this.flagTypeParser = Preconditions.checkNotNull(flagTypeParser);
    this.defaultValue = Preconditions.checkNotNull(defaultValue);
  }

  /**
   * Returns the name of this experiment flag, which should correspond to the name of
   * a parameter defined in the {@link ExperimentSpace}.
   *
   * @return the name of this flag
   */
  public String getName() {
    return name;
  }

  /**
   * Converts the given string into an instance of the type {@code T} of this flag.
   *
   * @param value the string form of the value
   * @return the value as an instance of type {@code T}
   */
  public T parse(String value) {
    return flagTypeParser.parse(value);
  }

  /**
   * Returns the default value of this flag, which is what will be returned when a call
   * to {@link ExperimentState#get(ExperimentFlag)} is made before the {@code ExperimentState}
   * has been diverted by the {@link ExperimentHandler}.
   *
   * @return the default value of this flag
   */
  public T getDefaultValue() {
    return defaultValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ExperimentFlag that = (ExperimentFlag) o;

    if (!defaultValue.equals(that.defaultValue)) return false;
    if (!flagTypeParser.equals(that.flagTypeParser)) return false;
    if (!name.equals(that.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + flagTypeParser.hashCode();
    result = 31 * result + defaultValue.hashCode();
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(flagTypeParser.toString()).append(" ").append(name).append(" = ").append(defaultValue);
    return sb.toString();
  } 
}

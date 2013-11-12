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
 * Defines a strategy for converting the string form of a type back into an instance of that type.
 */
public interface FlagTypeParser<T> {

  /**
   * Analyze the given value and return an instance of the type class for this instance.
   *
   * @param value the value to parse
   * @return a valid instance of the type class
   */
  T parse(CharSequence value);

  FlagTypeParser<Boolean> BOOLEAN_PARSER = new FlagTypeParser<Boolean>() {
    @Override
    public Boolean parse(CharSequence value) {
      return Boolean.valueOf(value.toString());
    }

    @Override
    public String toString() {
      return "bool";
    }
  };

  FlagTypeParser<Long> LONG_PARSER = new FlagTypeParser<Long>() {
    @Override
    public Long parse(CharSequence value) {
      return Long.valueOf(value.toString());
    }

    @Override
    public String toString() {
      return "long";
    }
  };

  FlagTypeParser<Double> DOUBLE_PARSER = new FlagTypeParser<Double>() {
    @Override
    public Double parse(CharSequence value) {
      return Double.valueOf(value.toString());
    }

    @Override
    public String toString() {
      return "double";
    }
  };

  FlagTypeParser<String> STRING_PARSER = new FlagTypeParser<String>() {
    @Override
    public String parse(CharSequence value) {
      return value.toString();
    }

    @Override
    public String toString() {
      return "string";
    }
  };

}

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

import com.cloudera.gertrude.FlagTypeParser;

public abstract class AssociativeOperator<T> {

  public abstract T apply(T baseValue, T nextValue);

  public abstract boolean isCommutative();

  public static <S> AssociativeOperator<S> get(String name, FlagTypeParser<S> parser) {
    if ("override".equalsIgnoreCase(name)) {
      return (AssociativeOperator<S>) OVERRIDE_OPERATOR;
    }
    if ("add".equalsIgnoreCase(name)) {
      if (parser == FlagTypeParser.BOOLEAN_PARSER) {
        return (AssociativeOperator<S>) ADD_BOOLEAN_OPERATOR;
      } else if (parser == FlagTypeParser.LONG_PARSER) {
        return (AssociativeOperator<S>) ADD_LONG_OPERATOR;
      } else if (parser == FlagTypeParser.DOUBLE_PARSER) {
        return (AssociativeOperator<S>) ADD_DOUBLE_OPERATOR;
      } else if (parser == FlagTypeParser.STRING_PARSER) {
        return (AssociativeOperator<S>) ADD_STRING_OPERATOR;
      } else {
        throw new IllegalArgumentException("Unsupported space type for add operator: " + parser);
      }
    }
    if ("multiply".equalsIgnoreCase(name)) {
      if (parser == FlagTypeParser.BOOLEAN_PARSER) {
        return (AssociativeOperator<S>) MULTIPLY_BOOLEAN_OPERATOR;
      } else if (parser == FlagTypeParser.LONG_PARSER) {
        return (AssociativeOperator<S>) MULTIPLY_LONG_OPERATOR;
      } else if (parser == FlagTypeParser.DOUBLE_PARSER) {
        return (AssociativeOperator<S>) MULTIPLY_DOUBLE_OPERATOR;
      } else {
        throw new IllegalArgumentException("Unsupported space type for multiply operator: " + parser);
      }
    }
    throw new IllegalArgumentException("Unknown binary operator: " + name);
  }

  static final AssociativeOperator<Object> OVERRIDE_OPERATOR = new AssociativeOperator<Object>() {
    @Override
    public Object apply(Object baseValue, Object nextValue) {
      return nextValue;
    }

    @Override
    public boolean isCommutative() {
      return false;
    }

    @Override
    public String toString() {
      return "override";
    }
  };

  static final AssociativeOperator<Boolean> ADD_BOOLEAN_OPERATOR = new AssociativeOperator<Boolean>() {
    @Override
    public Boolean apply(Boolean baseValue, Boolean nextValue) {
      return baseValue || nextValue;
    }

    @Override
    public boolean isCommutative() {
      return true;
    }

    @Override
    public String toString() {
      return "add";
    }
  };

  static final AssociativeOperator<Double> ADD_DOUBLE_OPERATOR = new AssociativeOperator<Double>() {
    @Override
    public Double apply(Double baseValue, Double nextValue) {
      return baseValue + nextValue;
    }

    @Override
    public boolean isCommutative() {
      return true;
    }

    @Override
    public String toString() {
      return "add";
    }
  };

  static final AssociativeOperator<Long> ADD_LONG_OPERATOR = new AssociativeOperator<Long>() {
    @Override
    public Long apply(Long baseValue, Long nextValue) {
      return baseValue + nextValue;
    }

    @Override
    public boolean isCommutative() {
      return true;
    }

    @Override
    public String toString() {
      return "add";
    }
  };

  static final AssociativeOperator<String> ADD_STRING_OPERATOR = new AssociativeOperator<String>() {
    @Override
    public String apply(String baseValue, String nextValue) {
      return baseValue + nextValue;
    }

    @Override
    public boolean isCommutative() {
      return true;
    }

    @Override
    public String toString() {
      return "add";
    }
  };

  static final AssociativeOperator<Boolean> MULTIPLY_BOOLEAN_OPERATOR = new AssociativeOperator<Boolean>() {
    @Override
    public Boolean apply(Boolean baseValue, Boolean nextValue) {
      return baseValue && nextValue;
    }

    @Override
    public boolean isCommutative() {
      return true;
    }

    @Override
    public String toString() {
      return "multiply";
    }
  };

  static final AssociativeOperator<Double> MULTIPLY_DOUBLE_OPERATOR = new AssociativeOperator<Double>() {
    @Override
    public Double apply(Double baseValue, Double nextValue) {
      return baseValue * nextValue;
    }

    @Override
    public boolean isCommutative() {
      return true;
    }

    @Override
    public String toString() {
      return "multiply";
    }
  };

  static final AssociativeOperator<Long> MULTIPLY_LONG_OPERATOR = new AssociativeOperator<Long>() {
    @Override
    public Long apply(Long baseValue, Long nextValue) {
      return baseValue * nextValue;
    }

    @Override
    public boolean isCommutative() {
      return true;
    }

    @Override
    public String toString() {
      return "multiply";
    }
  };
}

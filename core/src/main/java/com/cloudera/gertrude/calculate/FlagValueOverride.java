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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import java.util.List;

public abstract class FlagValueOverride<T> implements Function<FlagValueCalculatorImpl<T>, FlagValueCalculatorImpl<T>> {

  public static <T> FlagValueOverride<T> createReplace(final T baseValue, final List<Modifier<T>> modifiers) {
    return new FlagValueOverride<T>() {
      @Override
      public FlagValueCalculatorImpl<T> apply(FlagValueCalculatorImpl<T> input) {
        return new FlagValueCalculatorImpl<T>(baseValue, modifiers);
      }
    };
  }

  public static <S> FlagValueOverride<S> createAppend(final List<Modifier<S>> newModifiers) {
    return new FlagValueOverride<S>() {
      @Override
      public FlagValueCalculatorImpl<S> apply(FlagValueCalculatorImpl<S> input) {
        return new FlagValueCalculatorImpl<S>(input.getBaseValue(),
            ImmutableList.<Modifier<S>>builder()
                .addAll(input.getModifiers())
                .addAll(newModifiers)
                .build());
      }
    };
  }

  public static <S> FlagValueOverride<S> createPrepend(final List<Modifier<S>> newModifiers) {
    return new FlagValueOverride<S>() {
      @Override
      public FlagValueCalculatorImpl<S> apply(FlagValueCalculatorImpl<S> input) {
        return new FlagValueCalculatorImpl<S>(input.getBaseValue(),
            ImmutableList.<Modifier<S>>builder()
                .addAll(newModifiers)
                .addAll(input.getModifiers())
                .build());
      }
    };
  }
}

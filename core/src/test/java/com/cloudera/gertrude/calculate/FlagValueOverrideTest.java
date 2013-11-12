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

import com.cloudera.gertrude.ExperimentState;
import com.cloudera.gertrude.TestExperimentState;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public final class FlagValueOverrideTest {
  private final ExperimentState state = new TestExperimentState();

  private final List<Modifier<Double>> mods1 = ImmutableList.<Modifier<Double>>of(
    new BasicModifier<Double>(2.0, AssociativeOperator.ADD_DOUBLE_OPERATOR));
  private final FlagValueCalculatorImpl<Double> calc = new FlagValueCalculatorImpl<Double>(1.5, mods1);

  private final List<Modifier<Double>> mods2 = ImmutableList.<Modifier<Double>>of(
      new BasicModifier<Double>(0.0, AssociativeOperator.OVERRIDE_OPERATOR),
      new BasicModifier<Double>(1.0, AssociativeOperator.ADD_DOUBLE_OPERATOR));

  @Test
  public void testBasic() {
    assertEquals(3.5, calc.apply(state).getValue(), 0.001);
  }

  @Test
  public void testAppend() throws Exception {
    FlagValueOverride<Double> append = FlagValueOverride.createAppend(mods2);
    assertEquals(1.0, append.apply(calc).apply(state).getValue(), 0.001);
  }

  @Test
  public void testReplace() throws Exception {
    FlagValueOverride<Double> replace = FlagValueOverride.createReplace(0.5, mods1);
    assertEquals(2.5, replace.apply(calc).apply(state).getValue(), 0.001);
  }

  @Test
  public void testPrepend() throws Exception {
    FlagValueOverride<Double> prepend = FlagValueOverride.createPrepend(mods2);
    assertEquals(3.0, prepend.apply(calc).apply(state).getValue(), 0.001);
  }

}

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
import com.cloudera.gertrude.TestExperimentState;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public final class FlagValueCalculatorTest {

  private final ExperimentState state = new TestExperimentState();

  @Test
  public void testBasic() throws Exception {
    Modifier<Boolean> m1 = new BasicModifier<Boolean>(false, AssociativeOperator.ADD_BOOLEAN_OPERATOR);
    Modifier<Boolean> m2 = new BasicModifier<Boolean>(true, AssociativeOperator.MULTIPLY_BOOLEAN_OPERATOR);
    List<Modifier<Boolean>> mods = ImmutableList.of(m1, m2);

    FlagValueCalculatorImpl<Boolean> calc1 = new FlagValueCalculatorImpl<Boolean>(true, mods);
    FlagValueCalculatorImpl<Boolean> calc2 = new FlagValueCalculatorImpl<Boolean>(false, mods);
    assertEquals(FlagValue.of(true, Condition.CacheLevel.RELOAD), calc1.apply(state));
    assertEquals(FlagValue.of(false, Condition.CacheLevel.RELOAD), calc2.apply(state));
  }
}

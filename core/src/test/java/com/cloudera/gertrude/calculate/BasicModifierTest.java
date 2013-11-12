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

public final class BasicModifierTest {

  private final ExperimentState state = new TestExperimentState();

  @Test
  public void testSimple() {
    Modifier<Long> m = new BasicModifier<Long>(10L, AssociativeOperator.ADD_LONG_OPERATOR);
    FlagValue<Long> cv = FlagValue.of(7L, Condition.CacheLevel.REQUEST);
    m.apply(cv, state);
    assertEquals(FlagValue.of(17L, Condition.CacheLevel.REQUEST), cv);
  }

  @Test
  public void testNested() {
    List<Modifier<String>> mods = ImmutableList.<Modifier<String>>of(
        new BasicModifier<String>("bu", AssociativeOperator.OVERRIDE_OPERATOR),
        new BasicModifier<String>("zz", AssociativeOperator.ADD_STRING_OPERATOR));
    Modifier<String> m = new BasicModifier<String>("fizz", AssociativeOperator.ADD_STRING_OPERATOR,
        Condition.TRUE, mods);
    FlagValue<String> cv = FlagValue.of("fizz", Condition.CacheLevel.NONE);
    m.apply(cv, state);
    assertEquals(FlagValue.of("fizzbuzz", Condition.CacheLevel.NONE), cv);
  }
}

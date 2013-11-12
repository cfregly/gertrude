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
package com.cloudera.gertrude.condition;

import com.cloudera.gertrude.Condition;
import com.cloudera.gertrude.ExperimentState;
import com.cloudera.gertrude.TestExperimentState;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.*;

public final class BooleanConditionsTest {

  private final ExperimentState state = new TestExperimentState();

  @Test
  public void testNot() throws Exception {
    assertFalse(BooleanConditions.not(Condition.TRUE).evaluate(state));
    assertTrue(BooleanConditions.not(Condition.FALSE).evaluate(state));
    assertEquals(Condition.CacheLevel.RELOAD, BooleanConditions.not(Condition.TRUE).getCacheLevel());
  }

  @Test
  public void testOr() throws Exception {
    Condition c = BooleanConditions.or(ImmutableList.of(Condition.TRUE, Condition.FALSE));
    assertTrue(c.evaluate(state));
    assertEquals(Condition.CacheLevel.RELOAD, c.getCacheLevel());
  }


  @Test
  public void testAnd() throws Exception {
    Condition c = BooleanConditions.and(ImmutableList.of(Condition.TRUE, Condition.FALSE));
    assertFalse(c.evaluate(state));
    assertEquals(Condition.CacheLevel.RELOAD, c.getCacheLevel());
  }
}

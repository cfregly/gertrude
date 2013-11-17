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
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ConditionsTest {

  public static class TestPropertyCondition extends AbstractPropertyCondition<String, ExperimentState> {
    private String value;

    public void setValue(String value) {
      this.value = value;
    }

    @Override
    public CacheLevel getCacheLevel() {
      return CacheLevel.NONE;
    }

    @Override
    protected Set<String> parseArgs(List<String> args) {
      return ImmutableSet.copyOf(args);
    }

    @Override
    public String getValue(ExperimentState state) {
      return value;
    }
  }

  @Test
  public void testReflectConditionFactory() {
    ReflectionConditionFactory rcf = new ReflectionConditionFactory();
    rcf.register("test", TestPropertyCondition.class);
    Condition<ExperimentState> c = rcf.create("test");
    c.initialize(ImmutableList.of("foo", "bar"));
    assertTrue(c instanceof TestPropertyCondition);
    ((TestPropertyCondition) c).setValue("foo");
    ExperimentState state = new TestExperimentState();
    assertTrue(c.evaluate(state));

    ((TestPropertyCondition) c).setValue("bar");
    assertTrue(c.evaluate(state));

    ((TestPropertyCondition) c).setValue("baz");
    assertFalse(c.evaluate(state));
  }

  @Test
  public void testCompositeConditionFactory() {
    ReflectionConditionFactory rcf1 = new ReflectionConditionFactory();
    rcf1.register("foo", TestPropertyCondition.class);
    rcf1.register("bar", TestPropertyCondition.class);
    ReflectionConditionFactory rcf2 = new ReflectionConditionFactory();
    rcf2.register("baz", TestPropertyCondition.class);

    CompositeConditionFactory ccf = CompositeConditionFactory.create(rcf1, rcf2);
    assertEquals(ImmutableSet.of("foo", "bar", "baz"), ccf.supportedNames());
  }
}

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
package com.cloudera.gertrude.defaults;

import com.cloudera.gertrude.ExperimentFlag;
import com.cloudera.gertrude.ExperimentHandler;
import com.cloudera.gertrude.Experiments;
import com.cloudera.gertrude.TestExperimentState;
import com.cloudera.gertrude.TestExperiments;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class DefaultSettingsTest {

  private static final ExperimentFlag<String> strFlag = Experiments.declare("str", "foo");
  private static final ExperimentFlag<Long> longFlag = Experiments.declare("long", 1729);
  private static final ExperimentFlag<Double> doubleFlag = Experiments.declare("double", 17.29);
  private static final ExperimentFlag<Boolean> boolFlag = Experiments.declare("bool", false);

  @Test
  public void testNoHandler() throws Exception {
    TestExperimentState state = new TestExperimentState();
    assertEquals("foo", state.get(strFlag));
    assertEquals(1729L, state.get(longFlag).longValue());
    assertEquals(17.29, state.get(doubleFlag), 0.001);
    assertEquals(Boolean.FALSE, state.get(boolFlag));
    assertEquals(1729, state.getInt(longFlag));
    assertEquals(17.29f, state.getFloat(doubleFlag), 0.001f);

    assertTrue(state.getExperimentIds().isEmpty());
  }

  @Test
  public void testDefaultHandler() throws Exception {
    TestExperimentState state = new TestExperimentState();
    ExperimentHandler handler = TestExperiments.getHandler();
    handler.handle(state);

    assertEquals("foo", state.get(strFlag));
    assertEquals(1729L, state.get(longFlag).longValue());
    assertEquals(17.29, state.get(doubleFlag), 0.001);
    assertEquals(Boolean.FALSE, state.get(boolFlag));
    assertEquals(1729, state.getInt(longFlag));
    assertEquals(17.29f, state.getFloat(doubleFlag), 0.001f);

    assertTrue(state.getExperimentIds().isEmpty());
  }

  @Test
  public void testTestExperimentStateNoHandler() throws Exception {
    TestExperimentState state = new TestExperimentState()
        .set(strFlag, "bar")
        .set(longFlag, 17L)
        .set(boolFlag, true)
        .set(doubleFlag, 1.729);

    assertEquals("bar", state.get(strFlag));
    assertEquals(17L, state.get(longFlag).longValue());
    assertEquals(1.729, state.get(doubleFlag), 0.001);
    assertEquals(Boolean.TRUE, state.get(boolFlag));
    assertEquals(17, state.getInt(longFlag));
    assertEquals(1.729f, state.getFloat(doubleFlag), 0.001f);

    assertTrue(state.getExperimentIds().isEmpty());
  }

  @Test
  public void testTestExperimentStateWithHandler() throws Exception {
    TestExperimentState state = new TestExperimentState()
        .set(strFlag, "bar")
        .set(longFlag, 17L)
        .set(boolFlag, true)
        .set(doubleFlag, 1.729);
    TestExperiments.getHandler().handle(state);

    assertEquals("bar", state.get(strFlag));
    assertEquals(17L, state.get(longFlag).longValue());
    assertEquals(1.729, state.get(doubleFlag), 0.001);
    assertEquals(Boolean.TRUE, state.get(boolFlag));
    assertEquals(17, state.getInt(longFlag));
    assertEquals(1.729f, state.getFloat(doubleFlag), 0.001f);

    assertTrue(state.getExperimentIds().isEmpty());
  }
}

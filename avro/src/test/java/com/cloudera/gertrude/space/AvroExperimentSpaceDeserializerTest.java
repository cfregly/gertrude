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
package com.cloudera.gertrude.space;

import com.cloudera.gertrude.Condition;
import com.cloudera.gertrude.ExperimentFlag;
import com.cloudera.gertrude.Experiments;
import com.cloudera.gertrude.TestExperimentState;
import com.cloudera.gertrude.TestExperiments;
import com.cloudera.gertrude.condition.ReflectionConditionFactory;
import com.cloudera.gertrude.experiments.avro.ExperimentDefinition;
import com.cloudera.gertrude.experiments.avro.ExperimentDeployment;
import com.cloudera.gertrude.experiments.avro.ExperimentFlagDefinition;
import com.cloudera.gertrude.experiments.avro.FlagType;
import com.cloudera.gertrude.experiments.avro.ModifierOperator;
import com.cloudera.gertrude.experiments.avro.OverrideDefinition;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static com.cloudera.gertrude.space.AvroDataUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class AvroExperimentSpaceDeserializerTest {

  private static final ExperimentFlag<Long> foo = Experiments.declare("foo", 12L);
  private static final ExperimentFlag<String> bar = Experiments.declare("bar", "zzz");
  private static final ExperimentFlag<Boolean> baz = Experiments.declare("baz", false);
  private static final AvroExperimentSpaceDeserializer aedp = new AvroExperimentSpaceDeserializer(false);

  private final List<ExperimentFlagDefinition> flagDefs = ImmutableList.of(
      flagDef("foo", "17", FlagType.INT),
      flagDef("bar", "aaa", FlagType.STRING),
      flagDef("baz", "true", FlagType.BOOL));

  @BeforeClass
  public static void setUp() throws Exception {
    Map<String, ExperimentFlag<?>> flags = ImmutableMap.<String, ExperimentFlag<?>>of(
        "foo", foo, "bar", bar, "baz", baz);
    aedp.initialize(flags, new ReflectionConditionFactory());
  }

  @Test
  public void testJustFlagDefs() throws Exception {
    ExperimentDeployment deployment = new ExperimentDeployment();
    deployment.setFlagDefinitions(flagDefs);
    TestExperiments.setExperimentSpace(aedp.load(deployment, ""));
    TestExperimentState state = new TestExperimentState();
    TestExperiments.getHandler().handle(state);
    assertEquals(17L, state.get(foo).longValue());
    assertEquals("aaa", state.get(bar));
    assertEquals(Boolean.TRUE, state.get(baz));
  }

  @Test
  public void testEmptyLayer() throws Exception {
    int numBuckets = 100;
    ExperimentDeployment deployment = ExperimentDeployment.newBuilder()
        .setDiversions(ImmutableList.of(divDef(0, numBuckets, false)))
        .setFlagDefinitions(flagDefs)
        .setLayers(ImmutableList.of(layerDef(1, 0, false, 1)))
        .setExperiments(ImmutableList.<ExperimentDefinition>of())
        .build();
    TestExperiments.setExperimentSpace(aedp.load(deployment, ""));

    // Test unbiased assignment
    TestExperimentState state = new TestExperimentState().setDiversionIdentifier(0, "cookie");
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(1), state.getExperimentIds());
  }

  @Test
  public void testOneLayer() throws Exception {
    int numBuckets = 100;
    SortedSet<Integer> buckets = ImmutableSortedSet.of(82);
    SegmentInfo s1 = new SegmentInfo(10, 1, 0, buckets);
    ExperimentDeployment deployment = ExperimentDeployment.newBuilder()
        .setDiversions(ImmutableList.of(divDef(0, numBuckets, false)))
        .setFlagDefinitions(flagDefs)
        .setLayers(ImmutableList.of(layerDef(1, 0, false, 1)))
        .setExperiments(ImmutableList.of(exptDef(s1, 10)))
        .build();
    TestExperiments.setExperimentSpace(aedp.load(deployment, ""));

    // Test experiment assignment
    TestExperimentState state = new TestExperimentState().setDiversionIdentifier(0, "cookie");
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(10), state.getExperimentIds());
  }

  @Test
  public void testDisabled() throws Exception {
    int numBuckets = 100;
    SortedSet<Integer> buckets = ImmutableSortedSet.of(82);
    SegmentInfo s1 = new SegmentInfo(10, 1, 0, buckets);
    ExperimentDeployment deployment = ExperimentDeployment.newBuilder()
        .setDiversions(ImmutableList.of(divDef(0, numBuckets, false)))
        .setFlagDefinitions(flagDefs)
        .setLayers(ImmutableList.of(layerDef(1, 0, false, 1)))
        .setExperiments(ImmutableList.of(exptDef(s1, 10)))
        .build();
    TestExperiments.setExperimentSpace(aedp.load(deployment, ""));

    // Test experiment assignment
    TestExperimentState state = new TestExperimentState().setDiversionIdentifier(0, "cookie");
    TestExperiments.getHandler().disable(10);
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(1), state.getExperimentIds());
  }

  @Test
  public void testStartAndEndTimes() throws Exception {
    int numBuckets = 100;
    SortedSet<Integer> buckets = ImmutableSortedSet.of(82);
    SegmentInfo s1 = new SegmentInfo(10, 1, 0, buckets, Condition.TRUE, 50L, 100L, 50L, 100L);
    ExperimentDeployment deployment = ExperimentDeployment.newBuilder()
        .setDiversions(ImmutableList.of(divDef(0, numBuckets, false)))
        .setFlagDefinitions(flagDefs)
        .setLayers(ImmutableList.of(layerDef(1, 0, false, 1)))
        .setExperiments(ImmutableList.of(exptDef(s1, 10)))
        .build();
    TestExperiments.setExperimentSpace(aedp.load(deployment, ""));

    // Test experiment assignment
    TestExperimentState state = new TestExperimentState()
        .setDiversionIdentifier(0, "cookie");
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(1), state.getExperimentIds()); // unbiased identifier

    state = new TestExperimentState()
        .setDiversionIdentifier(0, "cookie")
        .setRequestTimeMsec(80L);
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(10), state.getExperimentIds()); // valid diversion


    state = new TestExperimentState()
        .setDiversionIdentifier(0, "cookie")
        .setRequestTimeMsec(120L);
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(1), state.getExperimentIds()); // unbiased identifier
  }

  @Test
  public void testPrePeriodPostPeriod() throws Exception {
    int numBuckets = 100;
    SortedSet<Integer> buckets = ImmutableSortedSet.of(82);
    SegmentInfo s1 = new SegmentInfo(10, 1, 0, buckets, Condition.TRUE, 0L, 100L, 20L, 60L);
    OverrideDefinition o1 = replaceDef("foo", "29");
    ExperimentDeployment deployment = ExperimentDeployment.newBuilder()
        .setDiversions(ImmutableList.of(divDef(0, numBuckets, false)))
        .setFlagDefinitions(flagDefs)
        .setLayers(ImmutableList.of(layerDef(1, 0, false, 1)))
        .setExperiments(ImmutableList.of(exptDef(s1, 10, o1)))
        .build();
    TestExperiments.setExperimentSpace(aedp.load(deployment, ""));

    // Test experiment assignment
    TestExperimentState state = new TestExperimentState()
        .setDiversionIdentifier(0, "cookie")
        .setRequestTimeMsec(10L);
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(10), state.getExperimentIds()); // preperiod
    assertEquals(17, state.getInt(foo));

    state = new TestExperimentState()
        .setDiversionIdentifier(0, "cookie")
        .setRequestTimeMsec(50L);
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(10), state.getExperimentIds()); // active experiment
    assertEquals(29, state.getInt(foo));

    state = new TestExperimentState()
        .setDiversionIdentifier(0, "cookie")
        .setRequestTimeMsec(80L);
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(10), state.getExperimentIds()); // postperiod
    assertEquals(17, state.getInt(foo));
  }

  @Test
  public void testUnbiasedAssignment() throws Exception {
    int numBuckets = 100;
    SortedSet<Integer> buckets = ImmutableSortedSet.of(82);
    SegmentInfo s1 = new SegmentInfo(10, 1, 0, buckets);
    ExperimentDeployment deployment = ExperimentDeployment.newBuilder()
        .setDiversions(ImmutableList.of(divDef(0, numBuckets, false)))
        .setFlagDefinitions(flagDefs)
        .setLayers(ImmutableList.of(layerDef(1, 0, false, 1)))
        .setExperiments(ImmutableList.of(exptDef(s1, 10)))
        .build();
    TestExperiments.setExperimentSpace(aedp.load(deployment, ""));

    // Test unbiased assignment
    TestExperimentState state = new TestExperimentState().setDiversionIdentifier(0, "mod");
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(1), state.getExperimentIds());
  }

  @Test
  public void testTwoLayers() throws Exception {
    int numBuckets = 100;
    //TODO: validate control experiment IDs
    SegmentInfo s1 = new SegmentInfo(10, 1, 0, ImmutableSortedSet.of(80));
    OverrideDefinition o1 = appendDef("foo", mod("2", ModifierOperator.MULTIPLY));
    SegmentInfo s2 = new SegmentInfo(20, 2, 0, ImmutableSortedSet.of(77));
    OverrideDefinition o2 = replaceDef("bar", "qqq");

    ExperimentDeployment deployment = ExperimentDeployment.newBuilder()
        .setDiversions(ImmutableList.of(divDef(0, numBuckets, false)))
        .setFlagDefinitions(flagDefs)
        .setLayers(ImmutableList.of(layerDef(1, 0, false, 1), layerDef(2, 0, false, 4)))
        .setExperiments(ImmutableList.of(exptDef(s1, 10, o1), exptDef(s2, 20, o2)))
        .build();
    TestExperiments.setExperimentSpace(aedp.load(deployment, ""));

    // Test experiment assignment
    TestExperimentState state = new TestExperimentState().setDiversionIdentifier(0, "mod");
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(10, 20), state.getExperimentIds());
    assertEquals(34, state.getInt(foo));
    assertEquals("qqq", state.get(bar));
  }

  @Test
  public void testLaunchLayer() throws Exception {
    int numBuckets = 100;
    SegmentInfo s1 = new SegmentInfo(10, 1, 0, ImmutableSortedSet.of(80));
    OverrideDefinition o1 = appendDef("foo", mod("2", ModifierOperator.MULTIPLY));

    ExperimentDeployment deployment = ExperimentDeployment.newBuilder()
        .setDiversions(ImmutableList.of(divDef(0, numBuckets, false)))
        .setFlagDefinitions(flagDefs)
        .setLayers(ImmutableList.of(layerDef(1, 0, true, 1)))
        .setExperiments(ImmutableList.of(exptDef(s1, 10, o1)))
        .build();
    TestExperiments.setExperimentSpace(aedp.load(deployment, ""));

    // Test experiment assignment
    TestExperimentState state = new TestExperimentState().setDiversionIdentifier(0, "mod");
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(10), state.getExperimentIds());
    assertEquals(34, state.getInt(foo));
  }

  @Test
  public void testTwoLayersWithLaunch() throws Exception {
    int numBuckets = 100;
    SegmentInfo s1 = new SegmentInfo(10, 1, 0, ImmutableSortedSet.of(80));
    OverrideDefinition o1 = replaceDef("foo", "29");
    SegmentInfo s2 = new SegmentInfo(20, 2, 0, ImmutableSortedSet.of(77));
    OverrideDefinition o2 = appendDef("foo", mod("2", ModifierOperator.MULTIPLY));

    ExperimentDeployment deployment = ExperimentDeployment.newBuilder()
        .setDiversions(ImmutableList.of(divDef(0, numBuckets, false)))
        .setFlagDefinitions(flagDefs)
        .setLayers(ImmutableList.of(layerDef(1, 0, true, 1), layerDef(2, 0, false, 4)))
        .setExperiments(ImmutableList.of(exptDef(s1, 10, o1), exptDef(s2, 20, o2)))
        .build();
    TestExperiments.setExperimentSpace(aedp.load(deployment, ""));

    // Test experiment assignment
    TestExperimentState state = new TestExperimentState().setDiversionIdentifier(0, "mod");
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(10, 20), state.getExperimentIds());
    assertEquals(29 * 2, state.getInt(foo));
  }

  @Test
  public void testTwoLayersWithIllegalOverrides() throws Exception {
    int numBuckets = 100;
    SegmentInfo s1 = new SegmentInfo(10, 1, 0, ImmutableSortedSet.of(80));
    OverrideDefinition o1 = replaceDef("foo", "29");
    SegmentInfo s2 = new SegmentInfo(20, 2, 0, ImmutableSortedSet.of(77));
    OverrideDefinition o2 = appendDef("foo", mod("2", ModifierOperator.MULTIPLY));

    ExperimentDeployment deployment = ExperimentDeployment.newBuilder()
        .setDiversions(ImmutableList.of(divDef(0, numBuckets, false)))
        .setFlagDefinitions(flagDefs)
        .setLayers(ImmutableList.of(layerDef(1, 0, false, 1), layerDef(2, 0, false, 4)))
        .setExperiments(ImmutableList.of(exptDef(s1, 10, o1), exptDef(s2, 20, o2)))
        .build();
    assertNull(aedp.load(deployment, ""));
  }

  @Test
  public void testTwoLayersWithValidOverrides() throws Exception {
    SortedSet<Integer> one = Sets.newTreeSet();
    SortedSet<Integer> two = Sets.newTreeSet();
    for (int i = 0; i < 100; i++) {
      if (i % 2 == 0) {
        one.add(i);
      } else {
        two.add(i);
      }
    }
    // Two domains within layer_id = 1, each with half of the traffic allocation
    SegmentInfo d1 = new SegmentInfo(10, 1, 0, one);
    SegmentInfo d2 = new SegmentInfo(20, 1, 0, two);

    // One experiment in layer_id = 2, which will live under domain_id = 10
    SegmentInfo s1 = new SegmentInfo(100, 2, 0, ImmutableSortedSet.of(77));
    OverrideDefinition o1 = replaceDef("foo", "29");
    // One experiment in layer_id = 3, which lives under domain_id = 20
    SegmentInfo s2 = new SegmentInfo(200, 3, 0, ImmutableSortedSet.of(77));
    OverrideDefinition o2 = prependDef("foo", mod("2", ModifierOperator.MULTIPLY));

    int numBuckets = 100;
    ExperimentDeployment deployment = ExperimentDeployment.newBuilder()
        .setDiversions(ImmutableList.of(divDef(0, numBuckets, false)))
        .setFlagDefinitions(flagDefs)
        .setLayers(ImmutableList.of(layerDef(1, 0, false, 1), layerDef(2, 10, false, 4), layerDef(3, 20, false, 7)))
        .setExperiments(ImmutableList.of(domainDef(d1), domainDef(d2), exptDef(s1, 100, o1), exptDef(s2, 200, o2)))
        .build();

    TestExperiments.setExperimentSpace(aedp.load(deployment, ""));

    // Test experiment assignment
    TestExperimentState state = new TestExperimentState().setDiversionIdentifier(0, "mod");
    TestExperiments.getHandler().handle(state);
    assertEquals(ImmutableSet.of(100), state.getExperimentIds());
    assertEquals(29, state.getInt(foo));
  }
}

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

import com.cloudera.gertrude.DiversionCriterion;
import com.cloudera.gertrude.ExperimentFlag;
import com.cloudera.gertrude.ExperimentSpace;
import com.cloudera.gertrude.Layer;
import com.cloudera.gertrude.Segment;
import com.cloudera.gertrude.calculate.FlagValueCalculatorImpl;
import com.cloudera.gertrude.calculate.FlagValueOverride;
import com.cloudera.gertrude.calculate.Modifier;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

final class ExperimentSpaceBuilder {

  private final Map<String, FlagValueCalculatorImpl<Object>> flagDefinitions = Maps.newHashMap();
  private final Map<Integer, SegmentInfo> allSegments = Maps.newHashMap();
  private final SortedMap<Integer, DiversionCriterion> diversionCriteria = Maps.newTreeMap();
  private final Map<Integer, LayerBuilder> layers = Maps.newHashMap();
  private final Multimap<Integer, LayerBuilder> layersByDomain = HashMultimap.create();
  private final SetMultimap<String, Integer> flagLayerAssignments = HashMultimap.create();
  private final Map<Integer, Map<String, FlagValueCalculatorImpl<Object>>> baseOverridesBySegment = Maps.newHashMap();

  private final Map<String, ExperimentFlag<?>> experimentFlags;
  private final Random random;

  ExperimentSpaceBuilder(Map<String, ExperimentFlag<?>> experimentFlags, Random random) {
    this.experimentFlags = experimentFlags;
    this.random = random;
  }

  void addFlagDefinition(String name, Object baseValue, List<Modifier<Object>> mods) {
    // First, check compiled flag definition if it exists.
    ExperimentFlag<?> compiledDef = experimentFlags.get(name);
    if (compiledDef != null) {
      // Compare the types of the compiled and external values
      if (!compiledDef.getDefaultValue().getClass().equals(baseValue.getClass())) {
        throw new IllegalStateException(String.format(
            "Compiled type does not match external type for flag: %s (%s vs. %s)", name,
            compiledDef.getDefaultValue().getClass(),
            baseValue.getClass()));
      }
    }
    // TODO: should note this mismatch unless we're in config mode

    // Next, ensure that this flag hasn't been defined yet.
    if (flagDefinitions.containsKey(name)) {
      throw new IllegalStateException("Cannot re-define experiment flag: " + name);
    } else {
      flagDefinitions.put(name, new FlagValueCalculatorImpl<Object>(baseValue, mods));
    }
  }

  void addDiversionCriterion(DiversionCriterion criteria) {
    if (diversionCriteria.containsKey(criteria.getId())) {
      throw new IllegalStateException("Cannot re-define diversion criteria: " + criteria.getId());
    }
    diversionCriteria.put(criteria.getId(), criteria);
  }

  void addLayer(LayerInfo info) {
    if (layers.containsKey(info.getLayerId())) {
      throw new IllegalStateException("Cannot re-define layer ID: " + info.getLayerId());
    }
    if (info.getDomainId() > 0 && info.isLaunchLayer()) {
      throw new IllegalStateException(
          "Launch layer " + info.getLayerId() + " can only be defined in the default domain (domain_id = 0)");
    }
    LayerBuilder lb = new LayerBuilder(this, info);
    layers.put(info.getLayerId(), lb);
    if (info.getDomainId() > 0) {
      layersByDomain.put(info.getDomainId(), lb);
    }
  }

  void addExperimentInfo(SegmentInfo info, boolean domain, Map<String, FlagValueOverride<Object>> overrides) {
    LayerBuilder layerBuilder = checkSegment(info);
    allSegments.put(info.getId(), info);
    if (domain) {
      layerBuilder.addDomain(info);
    } else {
      FlagValueData flagValueData = layerBuilder.checkOverrides(info.getId(), overrides);
      ExperimentInfo experimentInfo = new ExperimentInfo(info,
          flagValueData.baseOverrides,
          flagValueData.launchOverrides);
      layerBuilder.addExperiment(experimentInfo);
      baseOverridesBySegment.put(info.getId(), flagValueData.baseOverrides);
    }
  }

  ExperimentSpace build(String versionIdentifier) {
    List<Layer> ret = Lists.newArrayList();
    Map<Integer, Segment> finalSegments = Maps.newHashMap();
    for (LayerBuilder lb : layers.values()) {
      if (lb.getDomainId() == 0) {
        ret.add(lb.build(finalSegments));
      }
    }

    //TODO: build
    return new ExperimentSpace(
        versionIdentifier,
        flagDefinitions,
        finalSegments,
        Lists.newArrayList(diversionCriteria.values()),
        ret);
  }

  private Set<Integer> getLineage(int layerId) {
    ImmutableSet.Builder<Integer> b = ImmutableSet.builder();
    b.add(layerId);
    int domainId = layers.get(layerId).getDomainId();
    if (domainId > 0) {
      b.addAll(getLineage(allSegments.get(domainId).getLayerId()));
    }
    return b.build();
  }

  boolean areLayersOverlapping(int firstLayerId, int secondLayerId) {
    return Sets.intersection(getLineage(firstLayerId), getLineage(secondLayerId)).isEmpty();
  }

  private LayerBuilder checkSegment(SegmentInfo info) {
    if (allSegments.containsKey(info.getId())) {
      throw new IllegalStateException("Cannot re-define experiment ID: " + info.getId());
    }

    LayerBuilder layerBuilder = layers.get(info.getLayerId());
    if (layerBuilder == null) {
      throw new IllegalStateException("Undefined layer ID: " + info.getLayerId());
    }
    DiversionCriterion criteria = diversionCriteria.get(info.getDiversionId());
    if (criteria == null) {
      throw new IllegalStateException("Undefined diversion criteria ID: " + info.getDiversionId());
    }

    // Make sure we have room for this segment in the current allocation.
    layerBuilder.allocateBuckets(info.getId(), criteria, info.getBuckets());
    return layerBuilder;
  }

  Random getRandom() {
    return random;
  }

  FlagValueCalculatorImpl<Object> getFlagDefinition(String name) {
    return flagDefinitions.get(name);
  }

  LayerBuilder getLayer(int layerId) {
    return layers.get(layerId);
  }

  Set<Integer> getFlagLayerIds(String name) {
    return flagLayerAssignments.get(name);
  }

  void addFlagLayerAssignment(String name, int layerId) {
    flagLayerAssignments.put(name, layerId);
  }

  Domain getDomain(SegmentInfo info, Map<Integer, Segment> finalSegments) {
    Set<Layer> layers = Sets.newHashSet();
    for (LayerBuilder lb : layersByDomain.get(info.getId())) {
      layers.add(lb.build(finalSegments));
    }
    Domain domain = new Domain(info, layers);
    return domain;
  }

  FlagValueCalculatorImpl<Object> getFlagFromExperiment(int id, String name) {
    return baseOverridesBySegment.get(id).get(name);
  }
}

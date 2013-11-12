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
import com.cloudera.gertrude.Layer;
import com.cloudera.gertrude.Segment;
import com.cloudera.gertrude.calculate.FlagValueCalculatorImpl;
import com.cloudera.gertrude.calculate.FlagValueOverride;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedSet;

final class LayerBuilder {

  private final LayerInfo info;
  private final SetMultimap<String, Integer> flagOverridesByExperiment = HashMultimap.create();
  private final Map<Integer, ExperimentInfo> experiments = Maps.newHashMap();
  private final Map<Integer, SegmentInfo> domains = Maps.newHashMap();
  private final Map<Integer, Map<Integer, Integer>> allocatedBucketsByDiversion = Maps.newHashMap();
  private final ExperimentSpaceBuilder parent;

  LayerBuilder(ExperimentSpaceBuilder parent, LayerInfo info) {
    this.parent = parent;
    this.info = info;
  }

  boolean isLaunchLayer() {
    return info.isLaunchLayer();
  }

  int getDomainId() {
    return info.getDomainId();
  }

  void addDomain(SegmentInfo domain) {
    this.domains.put(domain.getId(), domain);
  }

  void addExperiment(ExperimentInfo info) {
    this.experiments.put(info.getId(), info);
  }

  Layer build(Map<Integer, Segment> finalSegments) {
    ImmutableMap.Builder<Integer, NavigableMap<Integer, Set<Segment>>> b = ImmutableMap.builder();
    Set<Integer> allSegmentIds = Sets.newHashSet();
    for (Map.Entry<Integer, Map<Integer, Integer>> e : allocatedBucketsByDiversion.entrySet()) {
      int diversionId = e.getKey();
      NavigableMap<Integer, Set<Segment>> bucketToSegment = Maps.newTreeMap();
      for (Map.Entry<Integer, Integer> ee : e.getValue().entrySet()) {
        int bucketId = ee.getKey();
        int segmentId = ee.getValue();
        Segment segment = experiments.get(segmentId);
        if (segment == null) {
          segment = parent.getDomain(domains.get(segmentId), finalSegments);
        } else {
          allSegmentIds.add(segmentId);
        }
        Set<Segment> segments = bucketToSegment.get(bucketId);
        if (segments == null) {
          segments = Sets.newHashSet();
          bucketToSegment.put(bucketId, segments);
        }
        segments.add(segment);
        finalSegments.put(segmentId, segment);
      }
      b.put(diversionId, bucketToSegment);
    }
    // TODO: check the collision properties of faster impls (e.g., MurmurHash3)
    return new LayerImpl(info, Hashing.md5(), allSegmentIds, b.build(), parent.getRandom());
  }

  void allocateBuckets(int segmentId, DiversionCriterion criteria, SortedSet<Integer> buckets) {
    if (buckets.first() < 0) {
      throw new IllegalStateException("Negative buckets in segment: " + segmentId);
    }
    if (buckets.last() >= criteria.getNumBuckets()) {
      throw new IllegalStateException("Buckets in segment " + segmentId + " exceeds max buckets for criteria");
    }
    Map<Integer, Integer> allocatedBuckets = allocatedBucketsByDiversion.get(criteria.getId());
    if (allocatedBuckets == null) {
      allocatedBuckets = Maps.newHashMap();
      allocatedBucketsByDiversion.put(criteria.getId(), allocatedBuckets);
    }
    Set<Integer> conflict = Sets.intersection(buckets, allocatedBuckets.keySet());
    if (!conflict.isEmpty()) {
      StringBuilder sb = new StringBuilder("Overlapping buckets for segment ")
          .append(segmentId)
          .append(" and segment(s) ");
      Set<Integer> conflictSegments = Sets.newHashSet();
      for (Integer bucket : conflict) {
        conflictSegments.add(allocatedBuckets.get(bucket));
      }
      sb.append(conflictSegments).append(" (Buckets: ").append(conflict).append(")");
      throw new IllegalStateException(sb.toString());
    }
    for (Integer bucket : buckets) {
      allocatedBuckets.put(bucket, segmentId);
    }
  }

  private Iterable<ExperimentInfo> findExperimentsThatOverride(String name) {
    return Iterables.transform(flagOverridesByExperiment.get(name), new Function<Integer, ExperimentInfo>() {
      @Override
      public ExperimentInfo apply(@Nullable Integer experimentId) {
        return experiments.get(experimentId);
      }
    });
  }

  FlagValueData checkOverrides(int experimentId, Map<String, FlagValueOverride<Object>> overrides) {
    ImmutableMap.Builder<String, FlagValueCalculatorImpl<Object>> bb = ImmutableMap.builder();
    Map<Integer, Map<String, FlagValueCalculatorImpl<Object>>> lb = Maps.newHashMap();
    for (Map.Entry<String, FlagValueOverride<Object>> e : overrides.entrySet()) {
      String name = e.getKey();
      FlagValueOverride<Object> valueOverride = e.getValue();
      FlagValueCalculatorImpl<Object> base = parent.getFlagDefinition(name);
      if (base == null) {
        throw new IllegalStateException("Experiment " + experimentId + " overrides non-existent flag " + name);
      }
      bb.put(name, valueOverride.apply(base));
      flagOverridesByExperiment.put(name, experimentId);

      //TODO: experiments can share experiment flags if they are in separate domains within a single layer
      Set<Integer> layerIds = parent.getFlagLayerIds(name);
      if (!layerIds.contains(info.getLayerId())) {
        if (!layerIds.isEmpty()) {
          if (isLaunchLayer()) {
            throw new IllegalStateException(String.format(
                "Experiments in multiple launch layers override flag %s: layer %d (experiment %d) and layer(s) %s",
                name, info.getLayerId(), experimentId, layerIds.toString()));
          } else {
            for (Integer otherLayerId : layerIds) {
              LayerBuilder other = parent.getLayer(otherLayerId);
              if (other.isLaunchLayer()) {
                for (ExperimentInfo info : other.findExperimentsThatOverride(name)) {
                  Map<String, FlagValueCalculatorImpl<Object>> llOverride = lb.get(info.getId());
                  if (llOverride == null) {
                    llOverride = Maps.newHashMap();
                    lb.put(info.getId(), llOverride);
                  }
                  llOverride.put(name, valueOverride.apply(parent.getFlagFromExperiment(info.getId(), name)));
                }
              } else if (parent.areLayersOverlapping(info.getLayerId(), otherLayerId)) {
                throw new IllegalStateException(String.format(
                    "Experiments across overlapping layers override flag %s: layer %d (experiment %d) and layer(s) %s",
                    name, info.getLayerId(), experimentId, layerIds.toString()));
              }
            }
          }
        }
        parent.addFlagLayerAssignment(name, info.getLayerId());
      }

    }
    return new FlagValueData(bb.build(), lb);
  }
}

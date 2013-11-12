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
import com.cloudera.gertrude.ExperimentState;
import com.cloudera.gertrude.FlagValueCalculator;
import com.cloudera.gertrude.Layer;
import com.cloudera.gertrude.Segment;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.hash.HashFunction;
import com.google.common.math.LongMath;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;

/**
 * The logic for diverting a state into an experimental condition within a
 * single layer.
 */
public final class LayerImpl implements Layer {

  private final LayerInfo info;
  private final HashFunction hash;
  private final Set<Integer> segmentIds;
  private final Map<Integer, NavigableMap<Integer, Set<Segment>>> segmentsByDiversionBuckets;
  private final Random random;

  public LayerImpl(
      LayerInfo info,
      HashFunction hash,
      Set<Integer> segmentIds,
      Map<Integer, NavigableMap<Integer, Set<Segment>>> segmentsByDiversionBuckets,
      Random random) {
    this.info = info;
    this.hash = hash;
    this.segmentIds = segmentIds;
    this.segmentsByDiversionBuckets = segmentsByDiversionBuckets;
    this.random = random;
  }


  @Override
  public boolean isLaunchLayer() {
    return info.isLaunchLayer();
  }

  private int computeBucket(String identifier, int numBuckets) {
    long hc = hash.newHasher().putInt(info.getLayerId()).putString(identifier).hash().asLong();
    return LongMath.mod(hc, numBuckets);
  }

  @Override
  public void assign(
      final ExperimentState state,
      List<DiversionCriterion> diversionCriteria,
      Map<String, FlagValueCalculator<Object>> overrides,
      Set<Integer> newExperimentIds) {
    if (!Sets.intersection(segmentIds, state.getExperimentIds()).isEmpty()) {
      // Diversion has already happened in this layer.
      return;
    }

    for (DiversionCriterion criteria : diversionCriteria) {
      int bucket = -1;
      if (criteria.isRandom()) {
        bucket = random.nextInt(criteria.getNumBuckets());
      } else {
        Optional<String> identifier = state.getDiversionIdentifier(criteria.getId());
        if (identifier.isPresent()) {
          bucket = computeBucket(identifier.get(), criteria.getNumBuckets());
        }
      }
      if (bucket != -1) {
        Set<Segment> selected = findSegments(segmentsByDiversionBuckets.get(criteria.getId()), bucket);
        if (!selected.isEmpty()) {
          Set<Segment> valid = Sets.filter(selected, new Predicate<Segment>() {
            @Override
            public boolean apply(Segment segment) {
              return segment.isValidFor(state);
            }
          });
          if (valid.isEmpty()) {
            // There were experiments for this bucket, but this request did not match any of them.
            // Mark the request with the appropriate bias identifier.
            newExperimentIds.add(criteria.isRandom() ? info.getRandomBiasedId() : info.getFixedBiasedId());
          } else if (valid.size() == 1) {
            // Divert the request into this segment
            Iterables.getOnlyElement(valid).handle(state, diversionCriteria, overrides, newExperimentIds);
          } else {
            // Bad news
            throw new IllegalStateException(String.format(
                "Multiple valid segments assigned to bucket %d in layer %d: %s",
                bucket, info.getLayerId(), valid.toString()));
          }
          return;
        }
      }
    }

    // Only reach this point if there were no matching experiments in this layer for the current request.
    newExperimentIds.add(info.getUnbiasedId());
  }

  static Set<Segment> findSegments(NavigableMap<Integer, Set<Segment>> segments, final int bucket) {
    if (segments != null && bucket >= 0) {
      Map.Entry<Integer, Set<Segment>> e = segments.floorEntry(bucket);
      if (e != null) {
        return Sets.filter(e.getValue(), new Predicate<Segment>() {
          @Override
          public boolean apply(Segment segment) {
            return segment.getBuckets().contains(bucket);
          }
        });
      }
    }
    return ImmutableSet.of();
  }
}

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
package com.cloudera.gertrude;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.InputSupplier;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines the space of {@link ExperimentFlagSettings}, {@link Layer}s and {@link Segment}s that are available
 * for an {@link ExperimentState} to be diverted into by the {@link ExperimentHandler}.
 *
 * <p>Clients should not use this class directly; the framework creates a new {@code ExperimentSpace} instance
 * by analyzing and validating the serialized definition of flags, layers, experiments, and domains that is
 * configured for this binary by the {@link ExperimentSpaceLoader} and {@link ExperimentSpaceDeserializer},
 * validates the configuration, and then swaps it in to the {@code ExperimentHandler} for serving new requests
 * based on any new experiment definitions.
 */
public final class ExperimentSpace {
  private final String versionIdentifier;
  private final ExperimentFlagSettings baseSettings;
  private final Map<Integer, Segment> allSegments;
  private final List<DiversionCriterion> diversionCriteria;
  private final List<Layer> launchLayers;
  private final List<Layer> permanentLayers;

  /**
   * A container for the serialized form of the configuration data used to create a new {@code ExperimentSpace}.
   * Instances of this class are provided by a subclass of {@link ExperimentSpaceLoader} and are processed into
   * a valid {@code ExperimentSpace} instance by a subclass of {@link ExperimentSpaceDeserializer}.
   */
  public static class Serialized {
    private final String versionIdentifier;
    private final List<InputSupplier<? extends InputStream>> serializedData;

    public Serialized(String versionIdentifier, InputSupplier<? extends InputStream> supplier) {
      this(versionIdentifier, ImmutableList.<InputSupplier<? extends InputStream>>of(supplier));
    }

    public Serialized(String versionIdentifier, List<InputSupplier<? extends InputStream>> serialized) {
      this.versionIdentifier = Preconditions.checkNotNull(versionIdentifier);
      this.serializedData = Preconditions.checkNotNull(serialized);
    }

    public String getVersionIdentifier() {
      return versionIdentifier;
    }

    public List<InputSupplier<? extends InputStream>> getSerializedData() {
      return serializedData;
    }
  }

  ExperimentSpace() {
    this("");
  }

  ExperimentSpace(String versionIdentifier) {
    this.versionIdentifier = versionIdentifier;
    this.baseSettings = new ExperimentFlagSettings();
    this.allSegments = ImmutableMap.of();
    this.diversionCriteria = ImmutableList.of();
    this.launchLayers = ImmutableList.of();
    this.permanentLayers = ImmutableList.of();
  }

  public ExperimentSpace(
      String versionIdentifier,
      Map<String, ? extends FlagValueCalculator<Object>> baseSettings,
      Map<Integer, Segment> allSegments,
      List<DiversionCriterion> diversionCriteria,
      List<Layer> allLayers) {
    this.versionIdentifier = versionIdentifier;
    this.baseSettings = new ExperimentFlagSettings(baseSettings);
    this.allSegments = ImmutableMap.copyOf(allSegments);
    this.diversionCriteria = ImmutableList.copyOf(diversionCriteria);
    this.launchLayers = Lists.newArrayList();
    this.permanentLayers = Lists.newArrayList();
    for (Layer layer : allLayers) {
      if (layer.isLaunchLayer()) {
        launchLayers.add(layer);
      } else {
        permanentLayers.add(layer);
      }
    }
  }

  String getVersionIdentifier() {
    return versionIdentifier;
  }

  boolean disable(int experimentId) {
    Segment s = allSegments.get(experimentId);
    if (s != null) {
      s.disable();
      return true;
    }
    return false;
  }

  void diversion(AbstractExperimentState state, Set<Integer> newExperimentIds) {
    if (state.forceExperimentIds().isEmpty()) {
      randomDiversion(state, newExperimentIds);
    } else {
      forceDiversion(state, newExperimentIds);
    }
  }

  private void randomDiversion(AbstractExperimentState state, Set<Integer> newExperimentIds) {
    ExperimentFlagSettings llSettings = assignFrom(launchLayers, state, baseSettings, newExperimentIds);
    state.setFlagSettings(assignFrom(permanentLayers, state, llSettings, newExperimentIds));
  }

  private void forceDiversion(AbstractExperimentState state, Set<Integer> experimentIds) {
    Map<String, FlagValueCalculator<Object>> overrides = Maps.newHashMap();
    for (int forceId : state.forceExperimentIds()) {
      Segment s = allSegments.get(forceId);
      if (s != null) {
        s.handle(state, diversionCriteria, overrides, experimentIds);
      }
    }
    state.setFlagSettings(baseSettings.withOverrides(overrides));
  }

  private ExperimentFlagSettings assignFrom(
      List<Layer> experimentsByLayer,
      ExperimentState state,
      ExperimentFlagSettings currentSettings,
      Set<Integer> experimentIds) {
    Map<String, FlagValueCalculator<Object>> overrides = Maps.newHashMap();
    for (Layer layer : experimentsByLayer) {
      layer.assign(state, diversionCriteria, overrides, experimentIds);
    }
    return currentSettings.withOverrides(overrides);
  }
}

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

import com.cloudera.gertrude.experiments.avro.DiversionDefinition;
import com.cloudera.gertrude.experiments.avro.ExperimentDefinition;
import com.cloudera.gertrude.experiments.avro.ExperimentFlagDefinition;
import com.cloudera.gertrude.experiments.avro.FlagType;
import com.cloudera.gertrude.experiments.avro.LayerDefinition;
import com.cloudera.gertrude.experiments.avro.ModifierDefinition;
import com.cloudera.gertrude.experiments.avro.ModifierOperator;
import com.cloudera.gertrude.experiments.avro.OverrideDefinition;
import com.cloudera.gertrude.experiments.avro.OverrideOperator;
import com.google.common.collect.Lists;

import java.util.Arrays;

public final class AvroDataUtils {

  private AvroDataUtils() {
  }

  public static ExperimentFlagDefinition flagDef(String name, String baseValue, FlagType flagType) {
    return ExperimentFlagDefinition.newBuilder()
        .setName(name)
        .setDescription("")
        .setBaseValue(baseValue)
        .setFlagType(flagType)
        .setModifiers(null)
        .build();
  }

  public static DiversionDefinition divDef(int diversionId, int numBuckets, boolean random) {
    return DiversionDefinition.newBuilder()
        .setId(diversionId)
        .setName("Diversion Criteria " + diversionId)
        .setNumBuckets(numBuckets)
        .setRandom(random)
        .build();
  }

  public static LayerDefinition layerDef(int layerId, int domainId, boolean launchLayer, int baseId) {
    return LayerDefinition.newBuilder()
        .setName("Layer " + layerId)
        .setId(layerId)
        .setDomainId(domainId)
        .setLaunch(launchLayer)
        .setUnbiasedId(baseId)
        .setFixedBiasedId(baseId + 1)
        .setRandomBiasedId(baseId + 2)
        .build();
  }

  public static ExperimentDefinition domainDef(SegmentInfo info) {
    return ExperimentDefinition.newBuilder()
        .setName("Domain " + info.getId())
        .setDescription("Domain " + info.getId())
        .setId(info.getId())
        .setControlId(info.getId())
        .setDomain(true)
        .setLayerId(info.getLayerId())
        .setDiversionId(info.getDiversionId())
        .setConditions(null)
        .setConditionMergeOperator(null)
        .setOwner("")
        .setOverrides(null)
        .setBucketRanges(null)
        .setBuckets(Lists.newArrayList(info.getBuckets()))
        .setStartTimeMsecUtc(info.getStartTimeMsec())
        .setEndTimeMsecUtc(info.getEndTimeMsec())
        .setPrePeriodMsecUtc(info.getPrePeriodTimeMsec())
        .setPostPeriodMsecUtc(info.getPostPeriodTimeMsec())
        .build();
  }

  public static ExperimentDefinition exptDef(SegmentInfo info, int controlId, OverrideDefinition... overrides) {
    return ExperimentDefinition.newBuilder()
        .setName("Domain " + info.getId())
        .setDescription("Domain " + info.getId())
        .setId(info.getId())
        .setControlId(controlId)
        .setDomain(false)
        .setLayerId(info.getLayerId())
        .setDiversionId(info.getDiversionId())
        .setConditions(null)
        .setConditionMergeOperator(null)
        .setOwner("")
        .setOverrides(Arrays.asList(overrides))
        .setBucketRanges(null)
        .setBuckets(Lists.newArrayList(info.getBuckets()))
        .setStartTimeMsecUtc(info.getStartTimeMsec())
        .setEndTimeMsecUtc(info.getEndTimeMsec())
        .setPrePeriodMsecUtc(info.getPrePeriodTimeMsec())
        .setPostPeriodMsecUtc(info.getPostPeriodTimeMsec())
        .build();
  }

  public static OverrideDefinition replaceDef(String flagName, String baseValue, ModifierDefinition... m) {
    return OverrideDefinition.newBuilder()
        .setName(flagName)
        .setOperator(OverrideOperator.REPLACE)
        .setBaseValue(baseValue)
        .setModifiers(Arrays.asList(m))
        .build();
  }

  public static <T> OverrideDefinition appendDef(String flagName, ModifierDefinition... m) {
    return OverrideDefinition.newBuilder()
        .setName(flagName)
        .setOperator(OverrideOperator.APPEND)
        .setBaseValue(null)
        .setModifiers(Arrays.asList(m))
        .build();
  }

  public static <T> OverrideDefinition prependDef(String flagName, ModifierDefinition... m) {
    return OverrideDefinition.newBuilder()
        .setName(flagName)
        .setOperator(OverrideOperator.PREPEND)
        .setBaseValue(null)
        .setModifiers(Arrays.asList(m))
        .build();
  }

  public static ModifierDefinition mod(String value, ModifierOperator op, ModifierDefinition... m) {
    return ModifierDefinition.newBuilder()
        .setValue(value)
        .setOperator(op)
        .setModifiers(Arrays.asList(m))
        .setConditionMergeOperator(null)
        .setConditions(null)
        .build();
  }
}

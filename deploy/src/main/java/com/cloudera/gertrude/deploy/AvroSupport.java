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
package com.cloudera.gertrude.deploy;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.cloudera.gertrude.experiments.avro.BucketRange;
import com.cloudera.gertrude.experiments.avro.ConditionDefinition;
import com.cloudera.gertrude.experiments.avro.ConditionOperator;
import com.cloudera.gertrude.experiments.avro.DiversionDefinition;
import com.cloudera.gertrude.experiments.avro.ExperimentDefinition;
import com.cloudera.gertrude.experiments.avro.ExperimentDeployment;
import com.cloudera.gertrude.experiments.avro.ExperimentFlagDefinition;
import com.cloudera.gertrude.experiments.avro.FlagType;
import com.cloudera.gertrude.experiments.avro.LayerDefinition;
import com.cloudera.gertrude.experiments.avro.ModifierDefinition;
import com.cloudera.gertrude.experiments.avro.ModifierOperator;
import com.cloudera.gertrude.experiments.avro.OverrideDefinition;
import com.cloudera.gertrude.experiments.avro.OverrideOperator;
import com.cloudera.gertrude.space.AvroExperimentSpaceDeserializer;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AvroSupport {

  @Parameter(names = "--skip-validation", description = "Skip validation checks on input config files.")
  private boolean skipValidation = false;

  @Parameter(names = "--timezone-id", description = "ID of the defaullt timezone to use for parsing datetime values")
  private String timeZoneId = DateTimeZone.getDefault().getID();

  @ParametersDelegate
  private ConditionFactorySupport conditionFactorySupport = new ConditionFactorySupport();

  @ParametersDelegate
  private ExperimentFlagSupport experimentFlagSupport = new ExperimentFlagSupport();

  private final DatumWriter<ExperimentDeployment> writer = new SpecificDatumWriter<ExperimentDeployment>(
      ExperimentDeployment.class);

  public AvroSupport() {}

  public AvroSupport(
      boolean skipValidation,
      ConditionFactorySupport conditionFactorySupport,
      ExperimentFlagSupport experimentFlagSupport) {
    this.skipValidation = skipValidation;
    this.conditionFactorySupport = conditionFactorySupport;
    this.experimentFlagSupport = experimentFlagSupport;
    DateTimeZone.setDefault(DateTimeZone.forID(timeZoneId));
  }

  public byte[] toBytes(ExperimentDeployment deployment) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
    writer.write(deployment, encoder);
    encoder.flush();
    return baos.toByteArray();
  }

  public void deploy(ExperimentDeployment deployment, String outputFile) throws Exception {
    DataFileWriter<ExperimentDeployment> dfw = new DataFileWriter<ExperimentDeployment>(writer)
        .create(ExperimentDeployment.getClassSchema(), new File(outputFile));
    dfw.append(deployment);
    dfw.close();
  }

  private List<? extends Config> getConfigList(Config base, String path, boolean required) {
    if (!base.hasPath(path)) {
      if (required) {
        throw new IllegalArgumentException(String.format("Config %s does not have required path: %s",
            base.resolve(), path));
      } else {
        return ImmutableList.of();
      }
    }
    return base.getConfigList(path);
  }

  public ExperimentDeployment createDeployment(Config base) throws IOException {
    ExperimentDeployment deployment = toExperimentDeployment(base);
    if (!skipValidation) {
      AvroExperimentSpaceDeserializer deserializer = new AvroExperimentSpaceDeserializer(false);
      deserializer.initialize(
          experimentFlagSupport.getExperimentFlags(),
          conditionFactorySupport.getConditionFactory());
      if (deserializer.load(deployment, "VALIDATIION") == null) {
        return null;
      }
    }
    return deployment;
  }

  private ExperimentDeployment toExperimentDeployment(Config base) {
    List<ExperimentDefinition> experimentDefinitions = Lists.newArrayList();
    experimentDefinitions.addAll(getExperiments(getConfigList(base, "EXPERIMENTS", false), false));
    if (base.hasPath("DOMAINS")) {
      experimentDefinitions.addAll(getExperiments(getConfigList(base, "DOMAINS", false), true));
    }

    return ExperimentDeployment.newBuilder()
        .setDiversions(getDiversions(getConfigList(base, "DIVERSIONS", true)))
        .setLayers(getLayers(getConfigList(base, "LAYERS", true)))
        .setExperiments(experimentDefinitions)
        .setFlagDefinitions(getFlags(getConfigList(base, "FLAGS", true)))
        .build();
  }

  private List<ExperimentFlagDefinition> getFlags(List<? extends Config> flagConfig) {
    return Lists.transform(flagConfig, new Function<Config, ExperimentFlagDefinition>() {
      @Override
      public ExperimentFlagDefinition apply(@Nullable Config input) {
        String flagName = input.getString("name");
        if (flagName == null) {
          throw new IllegalArgumentException("Invalid flag config, no name: " + input.resolve().toString());
        }
        return ExperimentFlagDefinition.newBuilder()
            .setName(flagName)
            .setBaseValue(input.getString("base-value"))
            .setDescription(input.getString("description"))
            .setFlagType(FlagType.valueOf(input.getString("flag-type").toUpperCase()))
            .setModifiers(getModifiers(getConfigList(input, "modifiers", false)))
            .build();
      }
    });
  }

  private List<ModifierDefinition> getModifiers(List<? extends Config> modifierList) {
    return Lists.transform(modifierList, new Function<Config, ModifierDefinition>() {
      @Override
      public ModifierDefinition apply(@Nullable Config input) {
        return ModifierDefinition.newBuilder()
            .setValue(input.getString("value"))
            .setConditions(getConditions(getConfigList(input, "conditions", false)))
            .setModifiers(getModifiers(getConfigList(input, "modifiers", false)))
            .setOperator(ModifierOperator.valueOf(input.getString("operator").toUpperCase()))
            .setConditionMergeOperator(getConditionOperator(input))
            .build();
      }
    });
  }

  private ConditionOperator getConditionOperator(Config input) {
    if (input.hasPath("condition-merge-operator")) {
      return ConditionOperator.valueOf(input.getString("condition-merge-operator").toUpperCase());
    }
    return null;
  }

  private List<ConditionDefinition> getConditions(List<? extends Config> conditionConfig) {
    return Lists.transform(conditionConfig, new Function<Config, ConditionDefinition>() {
      @Override
      public ConditionDefinition apply(@Nullable Config input) {
        List<CharSequence> args = Lists.newArrayList();
        if (input.hasPath("args")) {
          args.addAll(input.getStringList("args"));
        }
        boolean negate = input.hasPath("negate") && input.getBoolean("negate");
        return ConditionDefinition.newBuilder()
            .setName(input.getString("name"))
            .setArgs(args)
            .setNegate(negate)
            .build();
      }
    });
  }

  private List<ExperimentDefinition> getExperiments(List<? extends Config> experimentConfig, final boolean isDomain) {
    return Lists.transform(experimentConfig, new Function<Config, ExperimentDefinition>() {
      @Override
      public ExperimentDefinition apply(@Nullable Config input) {
        return ExperimentDefinition.newBuilder()
            .setName(input.getString("name"))
            .setDescription(input.getString("description"))
            .setOwner(input.getString("owner"))
            .setId(input.getInt("id"))
            .setControlId(input.getInt("control-id"))
            .setLayerId(input.getInt("layer-id"))
            .setDiversionId(input.getInt("diversion-id"))
            .setConditions(getConditions(getConfigList(input, "conditions", false)))
            .setConditionMergeOperator(getConditionOperator(input))
            .setDomain((input.hasPath("domain") && input.getBoolean("domain")) || isDomain)
            .setBuckets(input.hasPath("buckets") ? input.getIntList("buckets") : ImmutableList.<Integer>of())
            .setBucketRanges(getBucketRanges(getConfigList(input, "bucket-ranges", false)))
            .setStartTimeMsecUtc(getTime(input, "start-time"))
            .setEndTimeMsecUtc(getTime(input, "end-time"))
            .setPrePeriodMsecUtc(getTime(input, "preperiod-time"))
            .setPostPeriodMsecUtc(getTime(input, "postperiod-time"))
            .setOverrides(getOverrides(getConfigList(input, "overrides", false)))
            .build();
      }
    });
  }

  private Long getTime(Config config, String path) {
    if (!config.hasPath(path)) {
      return null;
    } else {
      return new DateTime(config.getString(path)).getMillis();
    }
  }

  private List<BucketRange> getBucketRanges(List<? extends Config> bucketRangeConfig) {
    return Lists.transform(bucketRangeConfig, new Function<Config, BucketRange>() {
      @Override
      public BucketRange apply(@Nullable Config input) {
        return BucketRange.newBuilder()
            .setStart(input.getInt("start"))
            .setEnd(input.getInt("end"))
            .build();
      }
    });
  }

  private List<OverrideDefinition> getOverrides(List<? extends Config> overrideConfig) {
    return Lists.transform(overrideConfig, new Function<Config, OverrideDefinition>() {
      @Override
      public OverrideDefinition apply(@Nullable Config input) {
        return OverrideDefinition.newBuilder()
            .setName(input.getString("name"))
            .setBaseValue(input.hasPath("base-value") ? input.getString("base-value") : "")
            .setModifiers(getModifiers(getConfigList(input, "modifiers", false)))
            .setOperator(OverrideOperator.valueOf(input.getString("operator").toUpperCase()))
            .build();
      }
    });
  }

  private List<LayerDefinition> getLayers(List<? extends Config> layerConfig) {
    return Lists.transform(layerConfig, new Function<Config, LayerDefinition>() {
      @Override
      public LayerDefinition apply(@Nullable Config input) {
        return LayerDefinition.newBuilder()
            .setName(input.getString("name"))
            .setId(input.getInt("id"))
            .setDomainId(input.hasPath("domain-id") ? input.getInt("domain-id") : 0)
            .setLaunch(input.hasPath("launch") && input.getBoolean("launch"))
            .setUnbiasedId(input.getInt("unbiased-id"))
            .setFixedBiasedId(input.getInt("fixed-biased-id"))
            .setRandomBiasedId(input.getInt("random-biased-id"))
            .build();
      }
    });
  }

  private List<DiversionDefinition> getDiversions(List<? extends Config> divConfig) {
    return Lists.transform(divConfig, new Function<Config, DiversionDefinition>() {
      @Override
      public DiversionDefinition apply(@Nullable Config input) {
        return DiversionDefinition.newBuilder()
            .setId(input.getInt("id"))
            .setName(input.getString("name"))
            .setNumBuckets(input.getInt("num-buckets"))
            .setRandom(input.hasPath("random") && input.getBoolean("random"))
            .build();
      }
    });
  }
}

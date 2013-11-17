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
import com.cloudera.gertrude.DiversionCriterion;
import com.cloudera.gertrude.ExperimentSpace;
import com.cloudera.gertrude.ExperimentSpaceDeserializer;
import com.cloudera.gertrude.ExperimentState;
import com.cloudera.gertrude.FlagTypeParser;
import com.cloudera.gertrude.calculate.AssociativeOperator;
import com.cloudera.gertrude.calculate.BasicModifier;
import com.cloudera.gertrude.calculate.FlagValueOverride;
import com.cloudera.gertrude.calculate.Modifier;
import com.cloudera.gertrude.condition.BooleanConditions;
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
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.FileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;

public class AvroExperimentSpaceDeserializer extends ExperimentSpaceDeserializer {

  private static final Logger log = LoggerFactory.getLogger(AvroExperimentSpaceDeserializer.class);

  private final DatumReader<ExperimentDeployment> reader;
  private final boolean avroFileInput;

  public AvroExperimentSpaceDeserializer(boolean avroFileInput) {
    this.reader = new SpecificDatumReader<ExperimentDeployment>(ExperimentDeployment.class);
    this.avroFileInput = avroFileInput;
  }

  @Override
  protected Optional<ExperimentSpace> deserialize(ExperimentSpace.Serialized serialized) throws IOException {
    ExperimentDeployment merged = null;
    ExperimentDeployment curr = null;
    if (avroFileInput) {
      for (InputSupplier<? extends InputStream> is : serialized.getSerializedData()) {
        SeekableInput si = new SeekableByteArrayInput(ByteStreams.toByteArray(is));
        FileReader<ExperimentDeployment> dfr = DataFileReader.openReader(si, reader);
        while (dfr.hasNext()) {
          merged = merge(merged, dfr.next(curr));
        }
      }
    } else {
      BinaryDecoder decoder = null;
      for (InputSupplier<? extends InputStream> is : serialized.getSerializedData()) {
        decoder = DecoderFactory.get().binaryDecoder(is.getInput(), decoder);
        merged = merge(merged, reader.read(curr, decoder));
      }
    }

    return merged == null ? Optional.<ExperimentSpace>absent() :
        Optional.fromNullable(load(merged, serialized.getVersionIdentifier()));
  }

  private static ExperimentDeployment merge(ExperimentDeployment one, ExperimentDeployment two) {
    if (one == null && two != null) {
      return two;
    } else {
      one.setFlagDefinitions(mergeLists(one.getFlagDefinitions(), two.getFlagDefinitions()));
      one.setDiversions(mergeLists(one.getDiversions(), two.getDiversions()));
      one.setLayers(mergeLists(one.getLayers(), two.getLayers()));
      one.setExperiments(mergeLists(one.getExperiments(), two.getExperiments()));
      return one;
    }
  }

  private static <S> List<S> mergeLists(List<S> one, List<S> two) {
    if (one == null) {
      one = Lists.newArrayList();
    }
    if (two != null) {
      one.addAll(two);
    }
    return one;
  }

  private static <S> List<S> emptyIfNull(List<S> list) {
    if (list == null) {
      return ImmutableList.of();
    }
    return list;
  }

  // Need to merge any existing deployment configs together before calling this
  public ExperimentSpace load(ExperimentDeployment deployment, String versionIdentifier) {
    if (deployment.getFlagDefinitions() == null || deployment.getFlagDefinitions().isEmpty()) {
      log.error("No flags defined in deployment");
      return null;
    }

    ExperimentSpaceBuilder builder = new ExperimentSpaceBuilder(getExperimentFlags(), new Random());
    Map<String, FlagTypeParser<Object>> parsers = Maps.newHashMap();
    for (ExperimentFlagDefinition flagDef : deployment.getFlagDefinitions()) {
      try {
        addFlagDefinition(flagDef, parsers, builder);
      } catch (ValidationException e) {
        log.error("Experiment flag validation error: {}\nFor input: {}", e, flagDef);
        return null;
      }
    }

    for (DiversionDefinition divDef : emptyIfNull(deployment.getDiversions())) {
      try {
        addDiversionCriterion(divDef, builder);
      } catch (ValidationException e) {
        log.error("Diversion criteria validation error: {}\nFor input: {}", e, divDef);
        return null;
      }
    }

    Set<Integer> configuredLayerIds = Sets.newHashSet();
    Set<Integer> configuredSegmentId = Sets.newHashSet();
    configuredSegmentId.add(0); // The default domain
    List<LayerDefinition> layerDefs = emptyIfNull(deployment.getLayers());
    while (configuredLayerIds.size() < layerDefs.size()) {
      boolean layersAdded = false;

      for (LayerDefinition layerDef : layerDefs) {
        if (configuredSegmentId.contains(layerDef.getDomainId()) &&
            !configuredLayerIds.contains(layerDef.getId())) {
          try {
            addLayer(layerDef, builder);
          } catch (ValidationException e) {
            log.error("Layer validation error: {}\nFor input: {}", e, layerDef);
            return null;
          }
          configuredLayerIds.add(layerDef.getId());
          layersAdded = true;
        }
      }

      if (!layersAdded) {
        throw new IllegalStateException("Invalid deployment configuration; infinite loop detected");
      }

      for (ExperimentDefinition exptDef : emptyIfNull(deployment.getExperiments())) {
        if (configuredLayerIds.contains(exptDef.getLayerId()) &&
            !configuredSegmentId.contains(exptDef.getId())) {
          try {
            addExperiment(exptDef, parsers, builder);
          } catch (ValidationException e) {
            log.error("Experiment validation error: {}\nFor input: {}", e, exptDef);
            return null;
          }
          configuredSegmentId.add(exptDef.getId());
        }
      }
    }

    return builder.build(versionIdentifier);
  }

  void addFlagDefinition(
      ExperimentFlagDefinition definition,
      Map<String, FlagTypeParser<Object>> parsers,
      ExperimentSpaceBuilder builder) throws ValidationException {
    String flagName = definition.getName().toString();
    FlagTypeParser<Object> parser = (FlagTypeParser<Object>) getParser(definition.getFlagType());
    builder.addFlagDefinition(
        flagName,
        parser.parse(definition.getBaseValue()),
        getModifiers(definition.getModifiers(), parser));
    parsers.put(flagName, parser);
  }

  static void addDiversionCriterion(DiversionDefinition diversion, ExperimentSpaceBuilder builder)
      throws ValidationException {
    DiversionCriterion dc = new DiversionCriterion(
        diversion.getId(),
        diversion.getNumBuckets(),
        diversion.getRandom());
    builder.addDiversionCriterion(dc);
  }

  static void addLayer(LayerDefinition layerDefinition, ExperimentSpaceBuilder builder)
      throws ValidationException {
    LayerInfo info = LayerInfo.builder(layerDefinition.getId())
        .domainId(layerDefinition.getDomainId())
        .launchLayer(layerDefinition.getLaunch())
        .unbiasedId(layerDefinition.getUnbiasedId())
        .fixedBiasedId(layerDefinition.getFixedBiasedId())
        .randomBiasedId(layerDefinition.getRandomBiasedId())
        .build();
    builder.addLayer(info);
  }

  void addExperiment(ExperimentDefinition exptDef,
                     Map<String, FlagTypeParser<Object>> parsers,
                     ExperimentSpaceBuilder builder) throws ValidationException {
    // Needs to be checked against existing bucket ranges
    SortedSet<Integer> buckets = getBuckets(exptDef.getBuckets(), exptDef.getBucketRanges());
    Condition<ExperimentState> condition = getCondition(exptDef.getConditions(), exptDef.getConditionMergeOperator());
    SegmentInfo info = new SegmentInfo(exptDef.getId(), exptDef.getLayerId(), exptDef.getDiversionId(),
        buckets, condition);
    Map<String, FlagValueOverride<Object>> overrides = getOverrides(exptDef.getOverrides(),
        parsers, exptDef.getId());
    builder.addExperimentInfo(info, exptDef.getDomain(), overrides);
  }

  static FlagTypeParser<?> getParser(FlagType flagType) throws ValidationException {
    switch (flagType) {
      case BOOL:
        return FlagTypeParser.BOOLEAN_PARSER;
      case INT:
        return FlagTypeParser.LONG_PARSER;
      case DOUBLE:
        return FlagTypeParser.DOUBLE_PARSER;
      case STRING:
        return FlagTypeParser.STRING_PARSER;
      default:
        throw new ValidationException("Unknown flag type: " + flagType);
    }
  }

  protected Map<String, FlagValueOverride<Object>> getOverrides(
      List<OverrideDefinition> overrides,
      Map<String, FlagTypeParser<Object>> parsers,
      int experimentId) throws ValidationException {
    ImmutableMap.Builder<String, FlagValueOverride<Object>> b = ImmutableMap.builder();
    if (overrides != null) {
      for (OverrideDefinition definition : overrides) {
        String flagName = definition.getName().toString();
        FlagTypeParser<Object> parser = parsers.get(flagName);
        if (parser == null) {
          throw new ValidationException(String.format(
              "Unknown experiment flag %s in experiment %d", flagName, experimentId));
        }
        List<Modifier<Object>> mods;
        try {
          mods = getModifiers(definition.getModifiers(), parser);
        } catch (ValidationException e) {
          throw new ValidationException(String.format(
              "Invalid modifier in overrides for flag %s in experiment %d", flagName, experimentId), e);
        }

        FlagValueOverride<Object> flagOverride;
        switch (definition.getOperator()) {
          case REPLACE:
            if (definition.getBaseValue() == null) {
              throw new ValidationException(String.format(
                  "REPLACE must have non-null base value for flag %s in experiment %d", flagName, experimentId));
            }
            Object baseValue = parser.parse(definition.getBaseValue());
            flagOverride = FlagValueOverride.createReplace(baseValue, mods);
            break;
          case APPEND:
            flagOverride = FlagValueOverride.createAppend(mods);
            break;
          case PREPEND:
            flagOverride = FlagValueOverride.createPrepend(mods);
            break;
          default:
            throw new ValidationException("Unknown override operator: " + definition.getOperator());
        }
        b.put(flagName, flagOverride);
      }
    }
    return b.build();
  }

  protected static SortedSet<Integer> getBuckets(List<Integer> buckets, List<BucketRange> bucketRanges) {
    SortedSet<Integer> ret = Sets.newTreeSet();
    if (buckets != null) {
      ret.addAll(buckets);
    }
    if (bucketRanges != null) {
      for (BucketRange br : bucketRanges) {
        for (int i = br.getStart(); i < br.getEnd(); i++) {
          ret.add(i);
        }
      }
    }
    return ret;
  }

  protected <T> List<Modifier<T>> getModifiers(List<ModifierDefinition> definitions, FlagTypeParser<T> parser)
      throws ValidationException{
    if (definitions == null || definitions.isEmpty()) {
      return ImmutableList.of();
    } else {
      List<Modifier<T>> modifiers = Lists.newArrayListWithExpectedSize(definitions.size());
      for (ModifierDefinition definition : definitions) {
        List<Modifier<T>> mods = definition.getModifiers() != null ?
            getModifiers(definition.getModifiers(), parser) :
            ImmutableList.<Modifier<T>>of();
        Condition<ExperimentState> condition =
            getCondition(definition.getConditions(), definition.getConditionMergeOperator());
        modifiers.add(new BasicModifier<T>(
            parser.parse(definition.getValue()),
            getOperatorFunction(definition.getOperator(), parser),
            condition,
            mods));
      }
      return modifiers;
    }
  }

  protected Condition<ExperimentState> getCondition(List<ConditionDefinition> definitions, ConditionOperator operator)
      throws ValidationException {
    if (definitions == null || definitions.isEmpty()) {
      return Condition.TRUE;
    } else {
      List<Condition<ExperimentState>> conditions = Lists.newArrayList();
      for (ConditionDefinition definition : definitions) {
        List<String> args = Lists.transform(emptyIfNull(definition.getArgs()), new Function<Object, String>() {
          @Override
          public String apply(Object in) {
            return in.toString();
          }
        });
        Condition<ExperimentState> c = getConditionFactory().create(definition.getName().toString());
        if (c != null) {
          try {
            c.initialize(args);
          } catch (Exception e) {
            throw new ValidationException("Exception initializing condition \"" + definition.getName() + "\"", e);
          }
          if (definition.getNegate() != null && definition.getNegate()) {
            c = BooleanConditions.not(c);
          }
          conditions.add(c);
        } else {
          throw new ValidationException("Unknown condition function name \"" + definition.getName() + "\"");
        }
      }
      // May want to cache the c + args mappings to the instances...
      if (conditions.size() == 1) {
        return conditions.get(0);
      } else if (operator == null || operator == ConditionOperator.AND) {
        return BooleanConditions.and(conditions);
      } else if (operator == ConditionOperator.OR) {
        return BooleanConditions.or(conditions);
      }
      throw new ValidationException("Unknown condition operator: " + operator);
    }
  }

  protected static <T> AssociativeOperator<T> getOperatorFunction(
      ModifierOperator operator,
      FlagTypeParser<T> parser) {
    return AssociativeOperator.get(operator.name(), parser);
  }
}

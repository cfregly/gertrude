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

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * A namespace for declaring, configuring, and accessing the core classes in the Gertrude framework.
 *
 * <p>Most clients will get started with Gertrude by declaring {@link ExperimentFlag} instances in
 * their code that reference parameters that can be calculated by the framework:
 * <pre>   {@code
 * ExperimentFlag<Boolean> featureOn = Experiments.declare("feature", false);
 * ExperimentFlag<Long> upperLimit = Experiments.declare("limit", 1729L);
 * ExperimentFlag<Double> threshold = Experiments.declare("threshold_for_model", 0.05);
 * ExperimentFlag<String> background = Experiments.declare("background_color", "white");  }</pre>
 *
 * <p>At server startup time, Gertrude needs some additional classes configured so that the framework
 * can load and process new experiments from external sources:
 * <ol>
 *   <li>A {@link ConditionFactory} for mapping from names of {@link Condition} functions to implementations,
 *   <li>a {@link ExperimentSpaceDeserializer} for processing the serialized form of an {@link ExperimentSpace},
 *   <li>a {@link ExperimentSpaceLoader} that is configured with the location of the serialized {@link ExperimentSpace},
 *   <li>and an optional {@link MetricRegistry} for tracking experiment requests and diversions.
 * </ol>
 * <p>After these instances are configured, the {@link ExperimentHandler} for the server can be accessed via the
 * {@link #getHandler()} method and used to divert client requests (represented by a subclass of
 * {@link AbstractExperimentState}) into experiments that modify parameter values as specified in the configuration
 * of the {@link ExperimentSpace}.
 */
public final class Experiments {
  private static final Map<String, ExperimentFlag<?>> EXPERIMENT_FLAGS = Maps.newHashMap();

  private static ConditionFactory CONDITION_FACTORY;
  private static ExperimentHandler HANDLER_INSTANCE;
  private static ExperimentSpaceLoader LOADER_INSTANCE;
  private static ExperimentSpaceDeserializer DESERIALIZER_INSTANCE;
  private static MetricRegistry METRIC_REGISTRY_INSTANCE;

  /**
   * Declares a boolean experiment flag with the given name and default value.
   *
   * @param name the name of the flag
   * @param defaultValue the default value of the flag
   * @return the instance of {@code ExperimentFlag} for the name and default value
   */
  public static ExperimentFlag<Boolean> declare(String name, boolean defaultValue) {
    return validate(new ExperimentFlag<Boolean>(name, FlagTypeParser.BOOLEAN_PARSER, defaultValue));
  }

  /**
   * Declares a long experiment flag with the given name and default value.
   *
   * @param name the name of the flag
   * @param defaultValue the default value of the flag
   * @return the instance of {@code ExperimentFlag} for the name and default value
   */
  public static ExperimentFlag<Long> declare(String name, long defaultValue) {
    return validate(new ExperimentFlag<Long>(name, FlagTypeParser.LONG_PARSER, defaultValue));
  }

  /**
   * Declares a double experiment flag with the given name and default value.
   *
   * @param name the name of the flag
   * @param defaultValue the default value of the flag
   * @return the instance of {@code ExperimentFlag} for the name and default value
   */
  public static ExperimentFlag<Double> declare(String name, double defaultValue) {
    return validate(new ExperimentFlag<Double>(name, FlagTypeParser.DOUBLE_PARSER, defaultValue));
  }

  /**
   * Declares a string experiment flag with the given name and default value.
   *
   * @param name the name of the flag
   * @param defaultValue the default value of the flag
   * @return the instance of {@code ExperimentFlag} for the name and default value
   */
  public static ExperimentFlag<String> declare(String name, String defaultValue) {
    return validate(new ExperimentFlag<String>(name, FlagTypeParser.STRING_PARSER, defaultValue));
  }

  /**
   * Register the given {@code ConditionFactory} for creating new {@link Condition} instances from the serialized
   * {@code ExperimentSpace} configuration.
   *
   * <p>If no factory is provided before the call to {@link #getHandler()}, an {@code IllegalStateException}
   * will be thrown.
   *
   * @param factory the configured factory
   * @return true if the given factory was configured, false if another one had already been configured
   */
  public static synchronized boolean registerConditionFactory(ConditionFactory factory) {
    if (CONDITION_FACTORY != null) {
      return false;
    }
    CONDITION_FACTORY = factory;
    return true;
  }

  /**
   * Register the given {@code ExperimentSpaceLoader} for retrieving serialized {@link ExperimentSpace}
   * instances.
   *
   * <p>If no loader is provided before the call to {@link #getHandler()}, an {@code IllegalStateException}
   * will be thrown.
   *
   * @param loader the configured loader
   * @return true if the given loader was configured, false if another one had already been configured
   */
  public static synchronized boolean registerLoader(ExperimentSpaceLoader loader) {
    if (LOADER_INSTANCE != null) {
      return false;
    }
    LOADER_INSTANCE = loader;
    return true;
  }

  /**
   * Register the given {@code ExperimentSpaceDeserializer} for creating and validating new {@link ExperimentSpace}
   * instances.
   *
   * <p>If no deserializer is provided before the call to {@link #getHandler()}, an {@code IllegalStateException}
   * will be thrown.
   *
   * @param deserializer the configured deserializer
   * @return true if the given deserializer was configured, false if another one had already been configured
   */
  public static synchronized boolean registerDeserializer(ExperimentSpaceDeserializer deserializer) {
    if (DESERIALIZER_INSTANCE != null) {
      return false;
    }
    DESERIALIZER_INSTANCE = deserializer;
    return true;
  }

  /**
   * Register the given {@code MetricRegistry} for stat tracking with the framework.
   *
   * <p>If no registry is provided before the call to {@link #getHandler()}, the framework will create its
   * own instance and use that one.
   *
   * @param registry the registry instance to use
   * @return true if the given registry was configured, false if another one had already been configured
   */
  public static synchronized boolean registerMetricRegistry(MetricRegistry registry) {
    if (METRIC_REGISTRY_INSTANCE != null) {
      return false;
    }
    METRIC_REGISTRY_INSTANCE = registry;
    return true;
  }

  public static synchronized ExperimentHandler getHandler() {
    if (HANDLER_INSTANCE == null) {
      if (LOADER_INSTANCE == null) {
        throw new IllegalStateException("No experiment space loader registered for the experiment handler");
      }
      if (DESERIALIZER_INSTANCE == null) {
        throw new IllegalStateException("No experiment space deserializer registered for the experiment handler");
      }
      if (CONDITION_FACTORY == null) {
        throw new IllegalStateException("No condition factory registered for the experiment handler");
      }
      if (METRIC_REGISTRY_INSTANCE == null) {
        METRIC_REGISTRY_INSTANCE = new MetricRegistry();
      }
      HANDLER_INSTANCE = new ExperimentHandler(METRIC_REGISTRY_INSTANCE);
      DESERIALIZER_INSTANCE.initialize(EXPERIMENT_FLAGS, CONDITION_FACTORY);
      // Initialize and load an initial state
      LOADER_INSTANCE.initialize(HANDLER_INSTANCE, DESERIALIZER_INSTANCE);
      LOADER_INSTANCE.reload(true);
    }
    return HANDLER_INSTANCE;
  }

  private static synchronized <T> ExperimentFlag<T> validate(ExperimentFlag<T> ret) {
    ExperimentFlag<?> existing = EXPERIMENT_FLAGS.get(ret.getName());
    if (existing != null) {
      if (ret.equals(existing)) {
        return (ExperimentFlag<T>) existing;
      } else {
        throw new IllegalStateException("Incompatible flags with the same name: " + existing + " and " + ret);
      }
    } else {
      EXPERIMENT_FLAGS.put(ret.getName(), (ExperimentFlag<Object>) ret);
      return ret;
    }
  }

  private Experiments() { }
}

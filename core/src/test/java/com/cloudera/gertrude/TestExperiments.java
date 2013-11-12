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

public final class TestExperiments {

  private static ExperimentHandler handler;
  private static ExperimentSpaceLoader loader = new TestExperimentSpaceLoader();
  private static ExperimentSpaceDeserializer deserializer = new TestExperimentSpaceDeserializer();
  private static ConditionFactory conditionFactory = new TestConditionFactory();
  private static MetricRegistry metrics = new MetricRegistry();

  public static void setLoader(ExperimentSpaceLoader l) {
    loader = l;
  }

  public static boolean setExperimentSpace(ExperimentSpace space) {
    if (deserializer instanceof TestExperimentSpaceDeserializer) {
      ((TestExperimentSpaceDeserializer) deserializer).setData(space);
      if (handler != null) {
        loader.reload(true);
      }
      return true;
    }
    return false;
  }

  public static ExperimentHandler getHandler() {
    if (handler == null) {
      Experiments.registerMetricRegistry(metrics);
      Experiments.registerLoader(loader);
      Experiments.registerDeserializer(deserializer);
      Experiments.registerConditionFactory(conditionFactory);
      handler = Experiments.getHandler();
    }
    return handler;
  }
}

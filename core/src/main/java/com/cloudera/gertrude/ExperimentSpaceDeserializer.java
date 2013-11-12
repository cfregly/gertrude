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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.Map;

/**
 * Converts an {@link ExperimentSpace.Serialized} instance to an {@link ExperimentSpace} and performs any other
 * validation and verification checks that are necessary to ensure that the {@code ExperimentSpace} is ready
 * to be used to serve requests.
 *
 * <p>Different binaries may want to handle data deserialization in different ways; the Gertrude framework
 * attempts to make as few assumptions as possible about dependencies, up to and including serialization
 * frameworks (such as protocol buffers, Apache Avro, etc.) Other modules in the Gertrude framework provide
 * concrete implementations of particular serialization formats and associated deserialization code for use
 * by clients.
 */
public abstract class ExperimentSpaceDeserializer {
  private Map<String, ExperimentFlag<?>> experimentFlags;
  private ConditionFactory conditionFactory;

  /**
   * Initialize this instance with the compiled experiment flags and {@code ConditionFactory} needed to
   * validate and parse the serialized {@code ExperimentSpace}.
   *
   * <p>This is only public for testing new serialization frameworks; clients should not use this method
   * directly.
   *
   * @param experimentFlags the experiment flags that have been registered with this binary
   * @param conditionFactory the {@code ConditionFactory} configured for this binary
   */
  @VisibleForTesting
  public void initialize(Map<String, ExperimentFlag<?>> experimentFlags, ConditionFactory conditionFactory) {
    if (this.experimentFlags == null && this.conditionFactory == null) {
      this.experimentFlags = Preconditions.checkNotNull(experimentFlags);
      this.conditionFactory = Preconditions.checkNotNull(conditionFactory);
    } else {
      throw new IllegalStateException("ExperimentSpaceDeserializer has already been initialized");
    }
  }

  protected Map<String, ExperimentFlag<?>> getExperimentFlags() {
    if (experimentFlags == null) {
      throw new IllegalStateException("ExperimentSpaceDeserializer has not been initialized");
    }
    return experimentFlags;
  }

  protected ConditionFactory getConditionFactory() {
    if (conditionFactory == null) {
      throw new IllegalStateException("ExperimentSpaceDeserializer has not been initialized");
    }
    return conditionFactory;
  }

  /**
   * Attempts to convert the {@code ExperimentSpace.Serialized} data to an {@code ExperimentSpace}, returning
   * {@link com.google.common.base.Optional#absent()} in the case that the deserialization could not be
   * performed.
   *
   * @param data the serialized form of an {@code ExperimentSpace}
   * @return a valid {@code ExperimentSpace} or an {@code Optional.absent()} instance
   * @throws IOException if there is an issue reading the serialized data
   */
  protected abstract Optional<ExperimentSpace> deserialize(ExperimentSpace.Serialized data) throws IOException;
}

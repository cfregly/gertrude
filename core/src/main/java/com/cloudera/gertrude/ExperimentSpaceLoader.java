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

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Manages loading and deserializing new {@link ExperimentSpace} instances and configuring the
 * {@link ExperimentHandler} to start using them once they have been verified and validated.
 *
 * <p>There are many ways to provide data updates to a running binary, such as monitoring a file
 * or watching a Zookeepeer node. Clients should configure an instance of this class with the
 * {@link Experiments} namespace at server startup time, and several implementations are provided
 * in other framework modules.
 */
public abstract class ExperimentSpaceLoader {

  private static final Logger log = LoggerFactory.getLogger(ExperimentSpaceLoader.class);

  private ExperimentHandler handler;
  private ExperimentSpaceDeserializer deserializer;

  void initialize(ExperimentHandler handler, ExperimentSpaceDeserializer deserializer) {
    this.handler = handler;
    this.deserializer = deserializer;
  }

  protected synchronized boolean reload(boolean force) {
    Optional<ExperimentSpace.Serialized> serialized = getSerialized();
    if (!serialized.isPresent()) {
      log.warn("No space returned from experiment space supplier, skipping reload");
      return false;
    }

    if (!force && serialized.get().getVersionIdentifier().equals(handler.getVersionIdentifier())) {
      log.info("Skipping reload because experiment space versions match: {}", serialized.get().getVersionIdentifier());
      return false;
    }

    Optional<ExperimentSpace> data;
    try {
      data = deserializer.deserialize(serialized.get());
    } catch (IOException e) {
      log.warn("Unable to reload space", e);
      return false;
    }

    if (data.isPresent()) {
      handler.update(data.get());
      return true;
    } else {
      log.warn("Deserializer could not convert serialized data to a new ExperimentSpace");
      return false;
    }
  }

  protected abstract Optional<ExperimentSpace.Serialized> getSerialized();
}

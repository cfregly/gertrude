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

public final class TestExperimentSpaceDeserializer extends ExperimentSpaceDeserializer {

  private ExperimentSpace data;

  public TestExperimentSpaceDeserializer() {
    this(null);
  }

  public TestExperimentSpaceDeserializer(ExperimentSpace data) {
    this.data = data;
  }

  public void setData(ExperimentSpace data) {
    this.data = data;
  }

  @Override
  protected Optional<ExperimentSpace> deserialize(ExperimentSpace.Serialized serialized) {
    return data == null ? Optional.of(new ExperimentSpace(serialized.getVersionIdentifier())) : Optional.of(data);
  }
}

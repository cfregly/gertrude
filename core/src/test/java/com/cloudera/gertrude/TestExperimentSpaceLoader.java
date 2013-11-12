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
import com.google.common.collect.ImmutableList;
import com.google.common.io.InputSupplier;

import java.io.InputStream;
import java.util.List;

public final class TestExperimentSpaceLoader extends ExperimentSpaceLoader {

  private String versionIdentifier;
  private List<InputSupplier<? extends InputStream>> suppliers;

  public TestExperimentSpaceLoader() {
    this("");
  }

  public TestExperimentSpaceLoader(String versionIdentifier) {
    this(versionIdentifier, ImmutableList.<InputSupplier<? extends InputStream>>of());
  }

  public TestExperimentSpaceLoader(String versionIdentifier, InputSupplier<? extends InputStream> supplier) {
    this(versionIdentifier, ImmutableList.<InputSupplier<? extends InputStream>>of(supplier));
  }

  public TestExperimentSpaceLoader(String versionIdentifier, List<InputSupplier<? extends InputStream>> suppliers) {
    this.versionIdentifier = versionIdentifier;
    this.suppliers = suppliers;
  }

  public TestExperimentSpaceLoader setVersionIdentifier(String versionIdentifier) {
    this.versionIdentifier = versionIdentifier;
    return this;
  }

  public TestExperimentSpaceLoader setSupplier(InputSupplier<? extends InputStream> supplier) {
    return setSuppliers(ImmutableList.<InputSupplier<? extends InputStream>>of(supplier));
  }

  public TestExperimentSpaceLoader setSuppliers(List<InputSupplier<? extends InputStream>> suppliers) {
    this.suppliers = suppliers;
    return this;
  }

  @Override
  protected Optional<ExperimentSpace.Serialized> getSerialized() {
    return Optional.of(new ExperimentSpace.Serialized(versionIdentifier, suppliers));
  }
}

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

public final class LayerInfo {
  private final int layerId;
  private final boolean launchLayer;
  private final int domainId;
  private final int unbiasedId;
  private final int fixedBiasedId;
  private final int randomBiasedId;

  private LayerInfo(int layerId, boolean launchLayer, int domainId, int unbiasedId, int fixedBiasedId, int randomBiasedId) {
    this.layerId = layerId;
    this.launchLayer = launchLayer;
    this.domainId = domainId;
    this.unbiasedId = unbiasedId;
    this.fixedBiasedId = fixedBiasedId;
    this.randomBiasedId = randomBiasedId;
  }

  public int getLayerId() {
    return layerId;
  }

  public boolean isLaunchLayer() {
    return launchLayer;
  }

  public int getDomainId() {
    return domainId;
  }

  public int getUnbiasedId() {
    return unbiasedId;
  }

  public int getFixedBiasedId() {
    return fixedBiasedId;
  }

  public int getRandomBiasedId() {
    return randomBiasedId;
  }

  public static Builder builder(int layerId) {
    return new Builder(layerId);
  }

  public static final class Builder {
    private final int layerId;
    private boolean launchLayer;
    private int domainId;
    private int unbiasedId;
    private int fixedBiasedId;
    private int randomBiasedId;

    public Builder(int layerId) {
      this.layerId = layerId;
    }

    public Builder launchLayer(boolean launchLayer) {
      this.launchLayer = launchLayer;
      return this;
    }

    public Builder domainId(int domainId) {
      this.domainId = domainId;
      return this;
    }

    public Builder unbiasedId(int unbiasedId) {
      this.unbiasedId = unbiasedId;
      return this;
    }

    public Builder fixedBiasedId(int fixedBiasedId) {
      this.fixedBiasedId = fixedBiasedId;
      return this;
    }

    public Builder randomBiasedId(int randomBiasedId) {
      this.randomBiasedId = randomBiasedId;
      return this;
    }

    public LayerInfo build() {
      return new LayerInfo(layerId, launchLayer, domainId, unbiasedId, fixedBiasedId, randomBiasedId);
    }
  }
}

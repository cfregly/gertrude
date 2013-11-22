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

import com.cloudera.gertrude.calculate.FlagValueCalculatorImpl;

import java.util.Map;

final class FlagValueData {

  private final Map<String, FlagValueCalculatorImpl<Object>> baseOverrides;
  private final Map<Integer, Map<String, FlagValueCalculatorImpl<Object>>> launchOverrides;

  FlagValueData(Map<String, FlagValueCalculatorImpl<Object>> baseOverrides,
                Map<Integer, Map<String, FlagValueCalculatorImpl<Object>>> launchOverrides) {
    this.baseOverrides = baseOverrides;
    this.launchOverrides = launchOverrides;
  }

  Map<String, FlagValueCalculatorImpl<Object>> getBaseOverrides() {
    return baseOverrides;
  }

  Map<Integer, Map<String, FlagValueCalculatorImpl<Object>>> getLaunchOverrides() {
    return launchOverrides;
  }

}

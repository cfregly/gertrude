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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class TestExperimentState extends AbstractExperimentState {

  private final Map<ExperimentFlag<?>, Object> testValues = Maps.newHashMap();
  private final Set<Integer> forceIds = Sets.newHashSet();
  private final Map<Integer, String> diversionIds = Maps.newHashMap();
  private Long requestTimeMsec;

  @Override
  public Optional<String> getDiversionIdentifier(int diversionId) {
    return Optional.fromNullable(diversionIds.get(diversionId));
  }

  public TestExperimentState setDiversionIdentifier(int diversionId, String identifier) {
    this.diversionIds.put(diversionId, identifier);
    return this;
  }

  @Override
  public Set<Integer> forceExperimentIds() {
    return forceIds;
  }

  public TestExperimentState forceExperimentIds(Integer... ids) {
    this.forceIds.clear();
    Collections.addAll(forceIds, ids);
    return this;
  }

  @Override
  public <T> T get(ExperimentFlag<T> flag) {
    if (testValues.containsKey(flag)) {
      return (T) testValues.get(flag);
    }
    return super.get(flag);
  }

  public <T> TestExperimentState set(ExperimentFlag<T> flag, T value) {
    testValues.put(flag, value);
    return this;
  }

  public TestExperimentState setRequestTimeMsec(Long requestTimeMsec) {
    this.requestTimeMsec = requestTimeMsec;
    return this;
  }

  @Override
  public long getRequestTimeMsec() {
    return requestTimeMsec == null ? super.getRequestTimeMsec() : requestTimeMsec;
  }
}

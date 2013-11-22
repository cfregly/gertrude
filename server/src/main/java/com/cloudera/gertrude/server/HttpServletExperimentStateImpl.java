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
package com.cloudera.gertrude.server;

import com.cloudera.gertrude.AbstractExperimentState;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * A simple implementation of {@link AbstractExperimentState} and {@link HttpServletExperimentState} that
 * diverts input requests based on one or more cookie names that are associated with a {@link HttpServletRequest}.
 */
class HttpServletExperimentStateImpl extends AbstractExperimentState implements HttpServletExperimentState {

  private final HttpServletRequest request;
  private final Map<Integer, String> diversionCookies;

  HttpServletExperimentStateImpl(HttpServletRequest request, List<String> diversionCookies) {
    this.request = Preconditions.checkNotNull(request);
    this.diversionCookies = indexDiversionCookies(diversionCookies, request.getCookies());
  }

  private static Map<Integer, String> indexDiversionCookies(List<String> diversionCookies, Cookie[] cookies) {
    ImmutableMap.Builder<Integer, String> b = ImmutableMap.builder();
    for (int i = 0; i < diversionCookies.size(); i++) {
      String cookieName = diversionCookies.get(i);
      for (Cookie c : cookies) {
        if (cookieName.equals(c.getName())) {
          b.put(i, c.getValue());
          break;
        }
      }
    }
    return b.build();
  }

  @Override
  public Optional<String> getDiversionIdentifier(int diversionId) {
    return Optional.fromNullable(diversionCookies.get(diversionId));
  }

  @Override
  public HttpServletRequest getRequest() {
    return request;
  }
}

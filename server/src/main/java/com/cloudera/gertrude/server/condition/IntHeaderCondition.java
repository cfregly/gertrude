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
package com.cloudera.gertrude.server.condition;

import com.cloudera.gertrude.condition.AbstractPropertyCondition;
import com.cloudera.gertrude.server.HttpServletExperimentState;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

/**
 * An example implementation of an {@link AbstractPropertyCondition} that retrieves values of a header parameter
 * from the {@code HttpServletRequest} associated with an {@link com.cloudera.gertrude.server.HttpServletExperimentState} implementation.
 */
public class IntHeaderCondition extends AbstractPropertyCondition<Integer, HttpServletExperimentState> {

  private String header;

  @Override
  protected Set<Integer> parseArgs(List<String> args) {
    if (args.size() < 2) {
      throw new IllegalStateException("IntHeaderCondition function must have at least two arguments");
    }
    // The first argument is the attribute to look for in the header
    this.header = args.get(0);

    // the second
    return ImmutableSet.copyOf(Lists.transform(args.subList(1, args.size()), new Function<String, Integer>() {
      @Override
      public Integer apply(String input) {
        return Integer.valueOf(input);
      }
    }));
  }

  @Override
  public Integer getValue(HttpServletExperimentState state) {
    return state.getRequest().getIntHeader(header);
  }

  @Override
  public CacheLevel getCacheLevel() {
    return CacheLevel.REQUEST;
  }
}

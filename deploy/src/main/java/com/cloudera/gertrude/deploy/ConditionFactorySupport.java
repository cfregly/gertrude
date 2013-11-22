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
package com.cloudera.gertrude.deploy;

import com.beust.jcommander.Parameter;
import com.cloudera.gertrude.Condition;
import com.cloudera.gertrude.ConditionFactory;
import com.cloudera.gertrude.ExperimentState;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ConditionFactorySupport {

  private static final Logger log = LoggerFactory.getLogger(ConditionFactorySupport.class);
  private static final Pattern COMMA = Pattern.compile(",");

  @Parameter(names = "--conditions-file",
      description = "A file containing the names of valid conditions, one per line, used for validation.")
  private String conditionsFile;

  public ConditionFactorySupport() {}

  public ConditionFactorySupport(String conditionsFile) {
    this.conditionsFile = conditionsFile;
  }

  public ConditionFactory getConditionFactory() throws IOException {
    if (conditionsFile == null) {
      return new DeployConditionFactory();
    }
    Map<String, Condition<ExperimentState>> conditions = Maps.newHashMap();
    for (String line : Files.readLines(new File(conditionsFile), Charsets.UTF_8)) {
      String[] pieces = COMMA.split(line);
      if (pieces.length == 0) {
        // Ignore
      } else if (pieces.length == 1) {
        // Just a condition function name
        conditions.put(pieces[0], Condition.TRUE);
      } else if (pieces.length == 3) {
        String name = pieces[0];
        int minArgs = Integer.valueOf(pieces[1]);
        int maxArgs = Integer.valueOf(pieces[2]);
        if (maxArgs < 0) {
          maxArgs = Integer.MAX_VALUE;
        }
        conditions.put(name, new ArityValidatingCondition(name, minArgs, maxArgs));
      } else {
        log.warn("Ignoring invalid conditions file line: {}", line);
      }
    }
    return new DeployConditionFactory(conditions);
  }

  private static class DeployConditionFactory implements ConditionFactory {

    private final Map<String, Condition<ExperimentState>> conditions;
    private final boolean strict;

    private DeployConditionFactory() {
      this(Maps.<String, Condition<ExperimentState>>newHashMap());
    }

    private DeployConditionFactory(Map<String, Condition<ExperimentState>> conditions) {
      this.conditions = conditions;
      this.strict = !conditions.isEmpty();
    }

    @Override
    public Set<String> supportedNames() {
      return conditions.keySet();
    }

    @Override
    public Condition<ExperimentState> create(String name) {
      if (!conditions.containsKey(name)) {
        if (strict) {
          return null;
        }
        conditions.put(name, Condition.TRUE);
      }
      return conditions.get(name);
    }
  }

}

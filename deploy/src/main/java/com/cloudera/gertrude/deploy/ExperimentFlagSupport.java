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
import com.cloudera.gertrude.ExperimentFlag;
import com.cloudera.gertrude.Experiments;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class ExperimentFlagSupport {

  private static final Logger log = LoggerFactory.getLogger(ExperimentFlagSupport.class);
  private static final Pattern COMMA = Pattern.compile(",");

  @Parameter(names = "--flags-file",
      description = "A CSV file of 'flag_name,flag_type' pairs used for validation.")
  private String experimentFlagFile;

  public ExperimentFlagSupport() {}

  public ExperimentFlagSupport(String experimentFlagFile) {
    this.experimentFlagFile = experimentFlagFile;
  }

  public Map<String, ExperimentFlag<?>> getExperimentFlags() throws IOException {
    if (experimentFlagFile == null) {
      return ImmutableMap.of();
    }
    ImmutableMap.Builder<String, ExperimentFlag<?>> b = ImmutableMap.builder();
    for (String line : Files.readLines(new File(experimentFlagFile), Charsets.UTF_8)) {
      String[] pieces = COMMA.split(line);
      if (pieces.length == 2) {
        String flagName = pieces[0];
        String flagType = pieces[1].toLowerCase(Locale.ENGLISH);
        if (flagType.startsWith("bool")) {
          b.put(flagName, Experiments.declare(flagName, false));
        } else if (flagType.startsWith("int") || "long".equals(flagType)) {
          b.put(flagName, Experiments.declare(flagName, 0));
        } else if ("double".equals(flagType)) {
          b.put(flagName, Experiments.declare(flagName, 0.0));
        } else if ("string".equals(flagType)) {
          b.put(flagName, Experiments.declare(flagName, ""));
        } else {
          throw new IllegalArgumentException("Unknown flag type: " + flagType);
        }
      } else {
        log.warn("Ignoring invalid line from flag file: {}", line);
      }
    }
    return b.build();
  }
}

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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;
import com.cloudera.gertrude.experiments.avro.ExperimentDeployment;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Cyclone {

  private static final Logger log = LoggerFactory.getLogger(Cyclone.class);

  @Parameter(names = "--input", description="The input config file to parse, in either JSON or HOCON format.",
      required=true)
  private String inputFile;

  @Parameter(names = "--output", description="The output destination, either a local file or Zookeeper path.",
      required=true)
  private String output;

  @Parameter(names = {"help", "-help", "-h", "--help"}, help=true, hidden=true)
  private boolean help = false;

  @ParametersDelegate
  private AvroSupport avroSupport = new AvroSupport();

  @ParametersDelegate
  private CuratorSupport curatorSupport = new CuratorSupport();

  private Cyclone() {}

  public Cyclone(String inputFile, String output, AvroSupport avroSupport, CuratorSupport curatorSupport) {
    this.inputFile = inputFile;
    this.output = output;
    this.avroSupport = avroSupport;
    this.curatorSupport = curatorSupport;
  }

  private int run(String[] args) throws Exception {
    JCommander jc = new JCommander(this);
    jc.setProgramName("cyclone");
    try {
      jc.parse(args);
    } catch (ParameterException e) {
      log.error(e.getLocalizedMessage());
      jc.usage();
      return 1;
    }

    if (help) {
      jc.usage();
      return 0;
    }

    Config config = ConfigFactory.parseFileAnySyntax(new File(inputFile));
    ExperimentDeployment deployment = avroSupport.createDeployment(config);
    if (deployment == null) {
      log.error("Could not create valid experiment deployment, exiting...");
      return 1;
    }
    if (curatorSupport.isEnabled()) {
      curatorSupport.deploy(avroSupport.toBytes(deployment), output);
    } else {
      avroSupport.deploy(deployment, output);
    }
    return 0;
  }

  public static void main(String[] args) throws Exception {
    new Cyclone().run(args);
  }
}

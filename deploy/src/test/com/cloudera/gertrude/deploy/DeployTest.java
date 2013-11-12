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

import com.cloudera.gertrude.experiments.avro.ExperimentDeployment;
import com.typesafe.config.ConfigFactory;
import org.apache.curator.test.TestingServer;
import org.junit.Test;

import java.io.File;

public class DeployTest {
  @Test
  public void testAvroFileDeploy() throws Exception {
    File tmpFile = File.createTempFile("experiments", ".avro");
    tmpFile.deleteOnExit();
    AvroSupport avroSupport = new AvroSupport();
    ExperimentDeployment ed = avroSupport.createDeployment(ConfigFactory.load("gertrude.conf"));
    avroSupport.deploy(ed, tmpFile.getAbsolutePath());
  }

  @Test
  public void testCuratorDeploy() throws Exception {
    TestingServer server = new TestingServer();

    AvroSupport avroSupport = new AvroSupport();
    CuratorSupport curator = new CuratorSupport(server.getConnectString());
    ExperimentDeployment ed = avroSupport.createDeployment(ConfigFactory.load("gertrude.conf"));
    String path = "/some/curator/path";
    curator.deploy(avroSupport.toBytes(ed), path);
  }
}

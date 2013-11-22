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
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

public class CuratorSupport {

  @Parameter(names = "--zk-connect", description="The connection string for the Zookeeper instance.")
  private String connectString;

  @Parameter(names = "--zk-retry-attempts", description="The number of times to retry a connection to Zookeeper.")
  private int retryTimes = 3;

  @Parameter(names = "--zk-retry-interval", description="The retry interval for Zookeeper, in milliseconds.")
  private int retryMs = 2000;

  public CuratorSupport() {}

  public CuratorSupport(String connectString) {
    this.connectString = connectString;
  }

  public boolean isEnabled() {
    return connectString != null;
  }

  public void deploy(byte[] deploymentData, String output) throws Exception {
    CuratorFramework client = CuratorFrameworkFactory.newClient(connectString,
        new RetryNTimes(retryTimes, retryMs));
    client.start();
    if (client.checkExists().forPath(output) == null) {
      client.create().creatingParentsIfNeeded().forPath(output);
    }
    client.setData().compressed().forPath(output, deploymentData);
    client.close();
  }
}

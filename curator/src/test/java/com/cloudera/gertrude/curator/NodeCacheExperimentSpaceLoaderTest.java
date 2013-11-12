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
package com.cloudera.gertrude.curator;

import com.cloudera.gertrude.ExperimentHandler;
import com.cloudera.gertrude.ExperimentSpace;
import com.cloudera.gertrude.TestExperiments;
import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeCacheExperimentSpaceLoaderTest {

  private static final String PATH = "/example/cache";

  @Test
  public void testNodeCache() throws Exception {
    TestingServer server = new TestingServer();
    byte[] data = "serializedExperimentData".getBytes();

    CuratorFramework client = CuratorFrameworkFactory.newClient(
        server.getConnectString(),
        new ExponentialBackoffRetry(500, 3));
    client.start();

    NodeCacheExperimentSpaceLoader loader = new NodeCacheExperimentSpaceLoader(client, PATH);
    TestExperiments.setLoader(loader);
    ExperimentHandler handler = TestExperiments.getHandler();

    Optional<ExperimentSpace.Serialized> serData = loader.getSerialized();
    assertFalse(serData.isPresent());
    assertEquals("", handler.getVersionIdentifier());

    client.create().creatingParentsIfNeeded().forPath(PATH);
    client.setData().compressed().forPath(PATH, data);
    Thread.sleep(1000L);

    serData = loader.getSerialized();
    assertEquals("1", serData.get().getVersionIdentifier());
    assertEquals(1, serData.get().getSerializedData().size());
    assertArrayEquals(data, ByteStreams.toByteArray(serData.get().getSerializedData().get(0)));
    assertEquals("1", handler.getVersionIdentifier());

    byte[] data2 = "otherSerializedExperimentData".getBytes();
    client.setData().compressed().forPath(PATH, data2);
    Thread.sleep(1000L);

    serData = loader.getSerialized();
    assertEquals("2", serData.get().getVersionIdentifier());
    assertEquals(1, serData.get().getSerializedData().size());
    assertArrayEquals(data2, ByteStreams.toByteArray(serData.get().getSerializedData().get(0)));
    assertEquals("2", handler.getVersionIdentifier());
  }
}

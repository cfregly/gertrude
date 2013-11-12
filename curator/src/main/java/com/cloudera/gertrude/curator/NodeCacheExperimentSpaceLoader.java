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

import com.cloudera.gertrude.ExperimentSpace;
import com.cloudera.gertrude.ExperimentSpaceLoader;
import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NodeCacheExperimentSpaceLoader extends ExperimentSpaceLoader {

  private static final Logger log = LoggerFactory.getLogger(NodeCacheExperimentSpaceLoader.class);

  private final NodeCache cache;

  public NodeCacheExperimentSpaceLoader(CuratorFramework client, String path) throws Exception {
    this.cache = new NodeCache(client, path, true /* compressed */);
    cache.getListenable().addListener(new NodeCacheListener() {
      @Override
      public void nodeChanged() throws Exception {
        log.info("Signaling space reload");
        cache.rebuild();
        reload(false);
      }
    });
    cache.start();
    cache.rebuild();
  }

  @Override
  protected Optional<ExperimentSpace.Serialized> getSerialized() {
    ChildData data = cache.getCurrentData();
    if (data == null) {
      log.warn("No data in node cache");
      return Optional.absent();
    } else {
      return Optional.of(new ExperimentSpace.Serialized(
          String.valueOf(data.getStat().getVersion()),
          ByteStreams.newInputStreamSupplier(data.getData())));
    }
  }
}

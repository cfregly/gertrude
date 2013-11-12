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
package com.cloudera.gertrude.file;

import com.cloudera.gertrude.ExperimentSpace;
import com.cloudera.gertrude.ExperimentSpaceLoader;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.FileFilter;

public final class FileExperimentSpaceLoader extends ExperimentSpaceLoader {
  private static final long DEFAULT_POLL_INTERVAL_MILLIS = 5 * 1000L;

  private final File dataFile;
  private final FileAlterationMonitor monitor;

  public FileExperimentSpaceLoader(File dataFile) throws Exception {
    this(dataFile, DEFAULT_POLL_INTERVAL_MILLIS);
  }

  public FileExperimentSpaceLoader(File dataFile, long pollIntervalMillis) throws Exception {
    Preconditions.checkArgument(!dataFile.isDirectory(), "Data file cannot be a directory");
    this.dataFile = dataFile;
    this.monitor = getMonitor(pollIntervalMillis);
    this.monitor.start();
  }

  private FileAlterationMonitor getMonitor(long pollIntervalMillis) {
    FileAlterationObserver observer = new FileAlterationObserver(dataFile.getParentFile(), new FileFilter() {
      @Override
      public boolean accept(File file) {
        return dataFile.equals(file);
      }
    });

    observer.addListener(new FileAlterationListenerAdaptor() {
      @Override
      public void onFileChange(File file) {
        file.setLastModified(System.currentTimeMillis());
        reload(false);
      }
    });
    FileAlterationMonitor m = new FileAlterationMonitor(pollIntervalMillis);
    m.addObserver(observer);
    return m;
  }

  @Override
  protected Optional<ExperimentSpace.Serialized> getSerialized() {
    return Optional.of(new ExperimentSpace.Serialized(
        String.valueOf(dataFile.lastModified()),
        Files.newInputStreamSupplier(dataFile)));
  }
}

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

import com.cloudera.gertrude.ExperimentHandler;
import com.cloudera.gertrude.ExperimentSpace;
import com.cloudera.gertrude.TestExperiments;
import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class FileExperimentSpaceLoaderTest {
  @Test
  public void testFileData() throws Exception {
    File tmpFile = File.createTempFile("space", "expt");
    tmpFile.deleteOnExit();

    FileExperimentSpaceLoader loader = new FileExperimentSpaceLoader(tmpFile, 1000L);
    TestExperiments.setLoader(loader);
    ExperimentHandler handler = TestExperiments.getHandler();

    Optional<ExperimentSpace.Serialized> serData = loader.getSerialized();
    assertEquals(1, serData.get().getSerializedData().size());
    assertArrayEquals(new byte[0], ByteStreams.toByteArray(serData.get().getSerializedData().get(0)));

    byte[] data = "serializedExperimentData".getBytes();
    Files.write(data, tmpFile);
    serData = loader.getSerialized();
    assertEquals(1, serData.get().getSerializedData().size());
    assertArrayEquals(data, ByteStreams.toByteArray(serData.get().getSerializedData().get(0)));
    String firstVersion = handler.getVersionIdentifier();
    assertNotEquals("", firstVersion);

    byte[] data2 = "otherSerializedExperimentData".getBytes();
    Files.write(data2, tmpFile);
    Thread.sleep(5000L);

    serData = loader.getSerialized();
    assertEquals(1, serData.get().getSerializedData().size());
    assertArrayEquals(data2, ByteStreams.toByteArray(serData.get().getSerializedData().get(0)));
    assertNotEquals(firstVersion, handler.getVersionIdentifier());
  }
}

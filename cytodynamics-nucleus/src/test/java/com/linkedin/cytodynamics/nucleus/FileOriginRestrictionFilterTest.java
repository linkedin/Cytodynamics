/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.io.File;
import org.junit.Test;

import static org.junit.Assert.*;


public class FileOriginRestrictionFilterTest {
  @Test
  public void testFileOrigin() throws Exception {
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    File directoryInTempDir = new File(tempDir, "test-" + System.currentTimeMillis());
    directoryInTempDir.mkdir();
    directoryInTempDir.deleteOnExit();

    // /tmp/test.jar
    File fileInTempDir = new File(tempDir, "test.jar");

    // /tmp/test-1234/test2.jar
    File fileInSubdirOfTempDir = new File(directoryInTempDir, "test2.jar");

    // /test3.jar
    File fileNotInTempDir = new File(tempDir.getParent(), "test3.jar");

    FileOriginRestrictionFilter nonRecursiveFilter = new FileOriginRestrictionFilter(tempDir, false, OriginMatchResults.ALLOW);
    assertEquals(OriginMatchResults.ALLOW, nonRecursiveFilter.isAllowed(fileInTempDir.toURI().toURL()));
    assertEquals(OriginMatchResults.NO_MATCH, nonRecursiveFilter.isAllowed(fileInSubdirOfTempDir.toURI().toURL()));
    assertEquals(OriginMatchResults.NO_MATCH, nonRecursiveFilter.isAllowed(fileNotInTempDir.toURI().toURL()));

    FileOriginRestrictionFilter recursiveFilter = new FileOriginRestrictionFilter(tempDir, true, OriginMatchResults.ALLOW);
    assertEquals(OriginMatchResults.ALLOW, recursiveFilter.isAllowed(fileInTempDir.toURI().toURL()));
    assertEquals(OriginMatchResults.ALLOW, recursiveFilter.isAllowed(fileInSubdirOfTempDir.toURI().toURL()));
    assertEquals(OriginMatchResults.NO_MATCH, recursiveFilter.isAllowed(fileNotInTempDir.toURI().toURL()));
  }
}

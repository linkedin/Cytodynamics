/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.io.File;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;


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
    assertEquals(nonRecursiveFilter.isAllowed(fileInTempDir.toURI().toURL()), OriginMatchResults.ALLOW);
    assertEquals(nonRecursiveFilter.isAllowed(fileInSubdirOfTempDir.toURI().toURL()), OriginMatchResults.NO_MATCH);
    assertEquals(nonRecursiveFilter.isAllowed(fileNotInTempDir.toURI().toURL()), OriginMatchResults.NO_MATCH);

    FileOriginRestrictionFilter recursiveFilter = new FileOriginRestrictionFilter(tempDir, true, OriginMatchResults.ALLOW);
    assertEquals(recursiveFilter.isAllowed(fileInTempDir.toURI().toURL()), OriginMatchResults.ALLOW);
    assertEquals(recursiveFilter.isAllowed(fileInSubdirOfTempDir.toURI().toURL()), OriginMatchResults.ALLOW);
    assertEquals(recursiveFilter.isAllowed(fileNotInTempDir.toURI().toURL()), OriginMatchResults.NO_MATCH);
  }
}

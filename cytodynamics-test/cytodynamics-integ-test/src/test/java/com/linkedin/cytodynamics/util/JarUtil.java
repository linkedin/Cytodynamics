/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;


public class JarUtil {
  private static final String CYTODYNAMICS_NUCLEUS = "cytodynamics-nucleus";

  public static URI getNucleusUri() throws IOException {
    return getJarUri(findBaseDirectory(), CYTODYNAMICS_NUCLEUS);
  }

  /**
   * Find the {@link URI} corresponding to the JAR for a test module.
   * This assumes that the test module is inside a module "cytodynamics-test", which is inside the top-level module.
   */
  public static URI getTestJarUri(String moduleName) throws IOException {
    File testDir = new File(findBaseDirectory(), "cytodynamics-test");
    return getJarUri(testDir, moduleName);
  }

  private static URI getJarUri(File moduleParent, String moduleName) {
    File targetDir = new File(new File(moduleParent, moduleName), "target");

    if (!targetDir.exists()) {
      throw new IllegalStateException(String.format("No target directory exists for module %s", moduleName));
    }

    File[] filesInTargetDir = targetDir.listFiles();
    if (filesInTargetDir == null) {
      throw new IllegalStateException(
          String.format("Unable to access any files in %s; make sure it is a directory", targetDir));
    }

    for (File fileInTargetDir : filesInTargetDir) {
      if (fileInTargetDir.getName().startsWith(moduleName) &&
          fileInTargetDir.getName().endsWith(".jar") &&
          !fileInTargetDir.getName().contains("sources") &&
          !fileInTargetDir.getName().contains("javadoc")) {
        return fileInTargetDir.toURI();
      }
    }

    throw new IllegalStateException(
        String.format("Failed to find the jar for module %s in the target directory %s", moduleName, targetDir));
  }

  private static File findBaseDirectory() throws IOException {
    File currentDirectory = new File(".").getCanonicalFile();
    File nucleusDirectory = new File(currentDirectory, CYTODYNAMICS_NUCLEUS);
    if (nucleusDirectory.exists() && nucleusDirectory.isDirectory()) {
      return currentDirectory;
    }

    while (currentDirectory.getParentFile() != null) {
      currentDirectory = currentDirectory.getParentFile();

      nucleusDirectory = new File(currentDirectory, CYTODYNAMICS_NUCLEUS);
      if (nucleusDirectory.exists() && nucleusDirectory.isDirectory()) {
        return currentDirectory;
      }
    }

    throw new IllegalStateException("Failed to find the base directory");
  }
}

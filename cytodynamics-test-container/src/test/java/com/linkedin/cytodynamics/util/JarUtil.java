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
  /**
   * Find the {@link URI} corersponding to the JAR for a module.
   */
  public static URI getJarUri(String moduleName) throws IOException {
    File targetDir = new File(new File(findBaseDirectory(), moduleName), "target");

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
    File testADirectory = new File(currentDirectory, "cytodynamics-test-a");
    if (testADirectory.exists() && testADirectory.isDirectory()) {
      return currentDirectory;
    }

    while (currentDirectory.getParentFile() != null) {
      currentDirectory = currentDirectory.getParentFile();

      testADirectory = new File(currentDirectory, "cytodynamics-test-a");
      if (testADirectory.exists() && testADirectory.isDirectory()) {
        return currentDirectory;
      }
    }

    throw new IllegalStateException("Failed to find the base directory");
  }
}

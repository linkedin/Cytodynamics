/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.io.File;
import java.net.URL;


/**
 * Origin restriction filter that uses a local filesystem path.
 */
public class FileOriginRestrictionFilter extends BaseOriginRestrictionFilter {
  private File directory;
  private final boolean recursive;

  protected FileOriginRestrictionFilter(File directory, boolean recursive, OriginMatchResults originMatchResults) {
    super(originMatchResults);
    this.directory = directory;
    this.recursive = recursive;
  }

  @Override
  public boolean matches(URL url) {
    if (!url.getProtocol().equals("file")) {
      return false;
    }

    try {
      File theFile = new File(url.toURI());
      if (!recursive) {
        return theFile.getParentFile().equals(directory);
      } else {
        File currentFile = theFile;
        while(currentFile.getParentFile() != null) {
          if (currentFile.getParentFile().equals(directory)) {
            return true;
          }

          currentFile = currentFile.getParentFile();
        }

        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }
}

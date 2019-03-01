/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.net.URL;


/**
 * Origin restriction filter that uses a glob pattern.
 */
class GlobPatternRestrictionFilter extends BaseOriginRestrictionFilter {
  private final GlobMatcher globMatcher;

  protected GlobPatternRestrictionFilter(String globPattern, OriginMatchResults originMatchResults) {
    super(originMatchResults);
    globMatcher = new GlobMatcher(globPattern);
  }

  @Override
  public boolean matches(URL url) {
    return globMatcher.matches(url.toExternalForm());
  }
}

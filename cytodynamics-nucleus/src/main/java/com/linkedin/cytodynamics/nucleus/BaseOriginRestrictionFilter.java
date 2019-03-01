/**
 * Copyright 2018 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.net.URL;


/**
 * Base implementation for origin filters.
 */
abstract class BaseOriginRestrictionFilter implements OriginRestrictionFilter {
  private final OriginMatchResults originMatchResults;

  protected BaseOriginRestrictionFilter(OriginMatchResults originMatchResults) {
    this.originMatchResults = originMatchResults;
  }

  public abstract boolean matches(URL url);

  public OriginMatchResults isAllowed(URL url) {
    if (matches(url)) {
      return originMatchResults;
    } else {
      return OriginMatchResults.NO_MATCH;
    }
  }
}

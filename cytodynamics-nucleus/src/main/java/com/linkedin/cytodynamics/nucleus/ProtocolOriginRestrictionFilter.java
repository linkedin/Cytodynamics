/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.net.URL;
import java.util.Set;


/**
 * Origin filter that filters based on the protocol (eg. http, https, file).
 */
class ProtocolOriginRestrictionFilter extends BaseOriginRestrictionFilter {
  private final Set<String> matchingProtocols;

  protected ProtocolOriginRestrictionFilter(Set<String> matchingProtocols, OriginMatchResults originMatchResults) {
    super(originMatchResults);
    this.matchingProtocols = matchingProtocols;
  }

  @Override
  public boolean matches(URL url) {
    return matchingProtocols.contains(url.getProtocol());
  }
}

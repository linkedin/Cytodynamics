/**
 * Copyright 2018 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


/**
 * Origin restriction, which allows restricting the origin of JARs present in the classpath.
 *
 * This is used to filter the original URL on the classpath, but does not take into account redirects. For example, if
 * the URL filter allows http://myhost.com/myjar.jar and that server redirects to http://malicious.com/evil.jar, then
 * the URL filter allows it.
 *
 * For finer control on the JARs loaded, we recommend denying by default and only allowing JARs downloaded at an
 * application-controlled location.
 */
public class OriginRestriction {
  private final boolean allowedByDefault;
  private List<OriginRestrictionFilter> filters = new ArrayList<>();

  private OriginRestriction(boolean allowedByDefault) {
    this.allowedByDefault = allowedByDefault;
  }

  public static OriginRestriction allowByDefault() {
    return new OriginRestriction(true);
  }

  public static OriginRestriction denyByDefault() {
    return new OriginRestriction(false);
  }

  public OriginRestriction allowingProtocols(String... protocols) {
    filters.add(new ProtocolOriginRestrictionFilter(new HashSet<>(Arrays.asList(protocols)),
        OriginMatchResults.ALLOW));

    return this;
  }

  public OriginRestriction denyingProtocols(String... protocols) {
    filters.add(new ProtocolOriginRestrictionFilter(new HashSet<>(Arrays.asList(protocols)),
        OriginMatchResults.DENY));

    return this;
  }

  public OriginRestriction allowingGlobPattern(String globPattern) {
    filters.add(new GlobPatternRestrictionFilter(globPattern, OriginMatchResults.ALLOW));

    return this;
  }

  public OriginRestriction denyingGlobPattern(String globPattern) {
    filters.add(new GlobPatternRestrictionFilter(globPattern, OriginMatchResults.DENY));

    return this;
  }

  public OriginRestriction allowingDirectory(File directory, boolean recursive) {
    filters.add(new FileOriginRestrictionFilter(directory, recursive, OriginMatchResults.ALLOW));

    return this;
  }

  public OriginRestriction denyingDirectory(File directory, boolean recursive) {
    filters.add(new FileOriginRestrictionFilter(directory, recursive, OriginMatchResults.DENY));

    return this;
  }

  boolean isAllowed(URL url) {
    OriginMatchResults lastCheckResults = null;

    // Find the first filter that matches
    for (OriginRestrictionFilter filter : filters) {
      lastCheckResults = filter.isAllowed(url);
      if (lastCheckResults.matches()) {
        break;
      }
    }

    // Return either the last successful match results or the default policy if no filter matched
    if (lastCheckResults != null && lastCheckResults.matches()) {
      return lastCheckResults.isAllowed();
    } else {
      return allowedByDefault;
    }
  }
}

/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.util.Set;


/**
 * Contains objects and configuration for an individual parent classloading step for {@link IsolatingClassLoader}.
 */
public class ParentClassLoaderContext {
  private final ClassLoader parentClassLoader;
  private final IsolationLevel isolationLevel;
  private final Set<GlobMatcher> parentPreferredClassPatterns;
  private final Set<GlobMatcher> blacklistedClassPatterns;
  private final Set<GlobMatcher> whitelistedClassPatterns;

  public ParentClassLoaderContext(ClassLoader parent,
      IsolationLevel isolationLevel,
      Set<GlobMatcher> parentPreferredClassPatterns,
      Set<GlobMatcher> blacklistedClassPatterns,
      Set<GlobMatcher> whitelistedClassPatterns) {
    this.parentClassLoader = parent;
    this.isolationLevel = isolationLevel;
    this.parentPreferredClassPatterns = parentPreferredClassPatterns;
    this.blacklistedClassPatterns = blacklistedClassPatterns;
    this.whitelistedClassPatterns = whitelistedClassPatterns;
  }

  public ClassLoader getParentClassLoader() {
    return parentClassLoader;
  }

  public IsolationLevel getIsolationLevel() {
    return isolationLevel;
  }

  public Set<GlobMatcher> getParentPreferredClassPatterns() {
    return parentPreferredClassPatterns;
  }

  public Set<GlobMatcher> getBlacklistedClassPatterns() {
    return blacklistedClassPatterns;
  }

  public Set<GlobMatcher> getWhitelistedClassPatterns() {
    return whitelistedClassPatterns;
  }
}

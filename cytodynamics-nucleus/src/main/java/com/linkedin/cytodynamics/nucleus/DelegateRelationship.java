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
public class DelegateRelationship {
  private final ClassLoader delegateClassLoader;
  private final IsolationLevel isolationLevel;
  private final Set<GlobMatcher> delegatePreferredClassPatterns;
  private final Set<GlobMatcher> blacklistedClassPatterns;
  private final Set<GlobMatcher> whitelistedClassPatterns;

  DelegateRelationship(ClassLoader delegateClassLoader, IsolationLevel isolationLevel,
      Set<GlobMatcher> delegatePreferredClassPatterns, Set<GlobMatcher> blacklistedClassPatterns,
      Set<GlobMatcher> whitelistedClassPatterns) {
    this.delegateClassLoader = delegateClassLoader;
    this.isolationLevel = isolationLevel;
    this.delegatePreferredClassPatterns = delegatePreferredClassPatterns;
    this.blacklistedClassPatterns = blacklistedClassPatterns;
    this.whitelistedClassPatterns = whitelistedClassPatterns;
  }

  public ClassLoader getDelegateClassLoader() {
    return delegateClassLoader;
  }

  public IsolationLevel getIsolationLevel() {
    return isolationLevel;
  }

  public Set<GlobMatcher> getDelegatePreferredClassPatterns() {
    return delegatePreferredClassPatterns;
  }

  public Set<GlobMatcher> getBlacklistedClassPatterns() {
    return blacklistedClassPatterns;
  }

  public Set<GlobMatcher> getWhitelistedClassPatterns() {
    return whitelistedClassPatterns;
  }
}

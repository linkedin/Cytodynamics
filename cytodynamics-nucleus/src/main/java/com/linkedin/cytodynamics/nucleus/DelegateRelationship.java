/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.util.Set;
import java.util.function.Predicate;


/**
 * Contains objects and configuration for an individual parent classloading step for {@link IsolatingClassLoader}.
 */
public class DelegateRelationship {
  private final ClassLoader delegateClassLoader;
  private final IsolationLevel isolationLevel;
  private final Set<Predicate<String>> delegatePreferredClassPredicates;
  private final Set<Predicate<String>> blacklistedClassPredicates;
  private final Set<Predicate<String>> whitelistedClassPredicates;
  private final Set<Predicate<String>> delegatePreferredResourcePredicates;
  private final Set<Predicate<String>> blacklistedResourcePredicates;
  private final Set<Predicate<String>> whitelistedResourcePredicates;

  DelegateRelationship(ClassLoader delegateClassLoader, IsolationLevel isolationLevel,
      Set<Predicate<String>> delegatePreferredClassPredicates,
      Set<Predicate<String>> blacklistedClassPredicates,
      Set<Predicate<String>> whitelistedClassPredicates,
      Set<Predicate<String>> delegatePreferredResourcePredicates,
      Set<Predicate<String>> blacklistedResourcePredicates,
      Set<Predicate<String>> whitelistedResourcePredicates) {
    this.delegateClassLoader = delegateClassLoader;
    this.isolationLevel = isolationLevel;
    this.delegatePreferredClassPredicates = delegatePreferredClassPredicates;
    this.blacklistedClassPredicates = blacklistedClassPredicates;
    this.whitelistedClassPredicates = whitelistedClassPredicates;
    this.delegatePreferredResourcePredicates = delegatePreferredResourcePredicates;
    this.blacklistedResourcePredicates = blacklistedResourcePredicates;
    this.whitelistedResourcePredicates = whitelistedResourcePredicates;
  }

  public ClassLoader getDelegateClassLoader() {
    return delegateClassLoader;
  }

  public IsolationLevel getIsolationLevel() {
    return isolationLevel;
  }

  public Set<Predicate<String>> getDelegatePreferredClassPredicates() {
    return delegatePreferredClassPredicates;
  }

  public Set<Predicate<String>> getBlacklistedClassPredicates() {
    return blacklistedClassPredicates;
  }

  public Set<Predicate<String>> getWhitelistedClassPredicates() {
    return whitelistedClassPredicates;
  }

  public Set<Predicate<String>> getDelegatePreferredResourcePredicates() {
    return delegatePreferredResourcePredicates;
  }

  public Set<Predicate<String>> getBlacklistedResourcePredicates() {
    return blacklistedResourcePredicates;
  }

  public Set<Predicate<String>> getWhitelistedResourcePredicates() {
    return whitelistedResourcePredicates;
  }
}

/**
 * Copyright 2018 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Loader builder fluent interface, used to build loaders with a given classpath and a specific level of isolation from
 * the parent's classloader.
 */
public final class LoaderBuilder {
  private IsolationLevel isolationLevel = IsolationLevel.NONE;
  private List<URI> classpath = new ArrayList<>();
  private Set<GlobMatcher> parentPreferredClassPatterns = new HashSet<>();
  private Set<GlobMatcher> blacklistedClassPatterns = new HashSet<>();
  private Set<GlobMatcher> whitelistedClassPatterns = new HashSet<>();
  private OriginRestriction originRestriction = null;

  private LoaderBuilder() {
  }

  public static LoaderBuilder anIsolatingLoader() {
    return new LoaderBuilder();
  }

  public LoaderBuilder withIsolationLevel(IsolationLevel isolationLevel) {
    this.isolationLevel = isolationLevel;
    return this;
  }

  public LoaderBuilder withClasspath(List<URI> classpath) {
    this.classpath.addAll(classpath);
    return this;
  }

  public LoaderBuilder withOriginRestriction(OriginRestriction originRestriction) {
    this.originRestriction = originRestriction;
    return this;
  }

  public LoaderBuilder addParentPreferredClassPattern(String pattern) {
    parentPreferredClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  public LoaderBuilder addBlacklistedClassPattern(String pattern) {
    blacklistedClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  public LoaderBuilder addWhitelistedClassPattern(String pattern) {
    whitelistedClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  public Loader build() {
    if (originRestriction == null) {
      throw new RuntimeException("No origin restriction set, use OriginRestriction.allowByDefault() if no restriction is desired");
    }

    return new IsolatingLoader(classpath, originRestriction, isolationLevel, parentPreferredClassPatterns, whitelistedClassPatterns, blacklistedClassPatterns);
  }
}

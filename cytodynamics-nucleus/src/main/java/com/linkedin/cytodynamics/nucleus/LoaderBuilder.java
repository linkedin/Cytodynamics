/**
 * Copyright 2018-2019 LinkedIn Corporation
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

  /**
   * Creates a blank loader builder.
   *
   * @return A new loader builder
   */
  public static LoaderBuilder anIsolatingLoader() {
    return new LoaderBuilder();
  }

  /**
   * Sets the isolation level of the loader. See {@link IsolationLevel} for more details on isolation levels.
   *
   * @param isolationLevel The isolation level of the loader.
   */
  public LoaderBuilder withIsolationLevel(IsolationLevel isolationLevel) {
    this.isolationLevel = isolationLevel;
    return this;
  }

  /**
   * Adds elements to the classpath of the loader.
   *
   * @param classpath The elements to add to the loader's classpath.
   */
  public LoaderBuilder withClasspath(List<URI> classpath) {
    this.classpath.addAll(classpath);
    return this;
  }

  /**
   * Sets the JAR origin restriction for JARs loaded by this loader. See {@link OriginRestriction} for more details on
   * JAR origin restrictions. For no restrictions, pass <code>OriginRestriction.allowByDefault()</code>.
   *
   * @param originRestriction The origin restriction to use
   */
  public LoaderBuilder withOriginRestriction(OriginRestriction originRestriction) {
    this.originRestriction = originRestriction;
    return this;
  }

  /**
   * Adds a glob pattern for classes to be loaded from the parent classloader as opposed to the child classloader. This
   * can be used to force the child to use certain classes which must have a common implementation, such as logging
   * libraries.
   *
   * @param pattern A glob pattern for classes to load from the parent classloader, such as "org.apache.log4j.*"
   */
  public LoaderBuilder addParentPreferredClassPattern(String pattern) {
    parentPreferredClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  /**
   * Adds a glob pattern for classes never to be loaded from the parent classloader, even if they have an
   * <code>@Api</code> annotation.
   *
   * @param pattern A glob pattern for classes to be avoided from the parent classloader
   */
  public LoaderBuilder addBlacklistedClassPattern(String pattern) {
    blacklistedClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  /**
   * Adds a glob pattern for classes to be allowed to be loaded from the parent classloader, if they don't exist in the
   * child classloader.
   *
   * @param pattern A glob pattern for classes to be allowed to be loaded from the parent classloader
   */
  public LoaderBuilder addWhitelistedClassPattern(String pattern) {
    whitelistedClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  /**
   * Builds an instance of a loader with the given parameters.
   *
   * @return A loader with the given parameters.
   */
  public Loader build() {
    if (originRestriction == null) {
      throw new RuntimeException("No origin restriction set, use OriginRestriction.allowByDefault() if no restriction is desired");
    }

    return new IsolatingLoader(classpath, originRestriction, isolationLevel, parentPreferredClassPatterns, whitelistedClassPatterns, blacklistedClassPatterns);
  }
}

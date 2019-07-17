/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.util.HashSet;
import java.util.Set;


/**
 * Parent relationship builder fluent interface, used to build {@link ParentRelationship} with a specific level of
 * isolation from a parent {@link ClassLoader}.
 */
public final class ParentRelationshipBuilder {
  private ClassLoader parentClassLoader = getClass().getClassLoader();
  private IsolationLevel isolationLevel = IsolationLevel.NONE;
  private Set<GlobMatcher> parentPreferredClassPatterns = new HashSet<>();
  private Set<GlobMatcher> blacklistedClassPatterns = new HashSet<>();
  private Set<GlobMatcher> whitelistedClassPatterns = new HashSet<>();

  private ParentRelationshipBuilder() {
  }

  /**
   * Creates a blank {@link ParentRelationshipBuilder}
   *
   * @return A new {@link ParentRelationshipBuilder}
   */
  public static ParentRelationshipBuilder builder() {
    return new ParentRelationshipBuilder();
  }

  /**
   * Sets the parent classloader for this parent relationship. By default, this is the classloader which loaded the
   * {@link ParentRelationshipBuilder} class.
   *
   * @param parentClassLoader parent classloader for the parent relationship
   */
  public ParentRelationshipBuilder withParentClassLoader(ClassLoader parentClassLoader) {
    this.parentClassLoader = parentClassLoader;
    return this;
  }

  /**
   * Sets the isolation level of the parent relationship. See {@link IsolationLevel} for more details on isolation
   * levels.
   *
   * @param isolationLevel The isolation level of the parent relationship.
   */
  public ParentRelationshipBuilder withIsolationLevel(IsolationLevel isolationLevel) {
    this.isolationLevel = isolationLevel;
    return this;
  }

  /**
   * Adds a glob pattern for classes to be loaded from the parent classloader as opposed to the child classloader. This
   * can be used to force the child to use certain classes which must have a common implementation, such as logging
   * libraries.
   *
   * @param pattern A glob pattern for classes to load from the parent classloader, such as "org.apache.log4j.*"
   */
  public ParentRelationshipBuilder addParentPreferredClassPattern(String pattern) {
    this.parentPreferredClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  /**
   * Adds a glob pattern for classes never to be loaded from the parent classloader, even if they have an
   * <code>@Api</code> annotation.
   *
   * @param pattern A glob pattern for classes to be avoided from the parent classloader
   */
  public ParentRelationshipBuilder addBlacklistedClassPattern(String pattern) {
    this.blacklistedClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  /**
   * Adds a glob pattern for classes to be allowed to be loaded from the parent classloader, if they don't exist in the
   * child classloader.
   *
   * @param pattern A glob pattern for classes to be allowed to be loaded from the parent classloader
   */
  public ParentRelationshipBuilder addWhitelistedClassPattern(String pattern) {
    this.whitelistedClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  /**
   * Builds an instance of a {@link ParentRelationship} with the given parameters.
   *
   * @return A {@link ParentRelationship} with the given parameters.
   */
  public ParentRelationship build() {
    return new ParentRelationship(this.parentClassLoader, this.isolationLevel, this.parentPreferredClassPatterns,
        this.blacklistedClassPatterns, this.whitelistedClassPatterns);
  }
}

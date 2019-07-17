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
 * Delegate relationship builder fluent interface, used to build {@link DelegateRelationship} with a specific level of
 * isolation from a delegate {@link ClassLoader}.
 */
public final class DelegateRelationshipBuilder {
  private ClassLoader delegateClassLoader = getClass().getClassLoader();
  private IsolationLevel isolationLevel = IsolationLevel.NONE;
  private Set<GlobMatcher> delegatePreferredClassPatterns = new HashSet<>();
  private Set<GlobMatcher> blacklistedClassPatterns = new HashSet<>();
  private Set<GlobMatcher> whitelistedClassPatterns = new HashSet<>();

  private DelegateRelationshipBuilder() {
  }

  /**
   * Creates a blank {@link DelegateRelationshipBuilder}
   *
   * @return A new {@link DelegateRelationshipBuilder}
   */
  public static DelegateRelationshipBuilder builder() {
    return new DelegateRelationshipBuilder();
  }

  /**
   * Sets the delegate classloader for this delegate relationship. By default, this is the classloader which loaded the
   * {@link DelegateRelationshipBuilder} class.
   *
   * @param delegateClassLoader delegate classloader for the relationship
   */
  public DelegateRelationshipBuilder withDelegateClassLoader(ClassLoader delegateClassLoader) {
    this.delegateClassLoader = delegateClassLoader;
    return this;
  }

  /**
   * Sets the isolation level of the parent relationship. See {@link IsolationLevel} for more details on isolation
   * levels.
   *
   * @param isolationLevel The isolation level of the parent relationship.
   */
  public DelegateRelationshipBuilder withIsolationLevel(IsolationLevel isolationLevel) {
    this.isolationLevel = isolationLevel;
    return this;
  }

  /**
   * Adds a glob pattern for classes to be loaded from the delegate classloader as opposed to the child classloader.
   * This can be used to force the child to use certain classes which must have a common implementation, such as logging
   * libraries.
   *
   * @param pattern A glob pattern for classes to load from the parent classloader, such as "org.apache.log4j.*"
   */
  public DelegateRelationshipBuilder addDelegatePreferredClassPattern(String pattern) {
    this.delegatePreferredClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  /**
   * Adds a glob pattern for classes never to be loaded from the parent classloader, even if they have an
   * <code>@Api</code> annotation.
   *
   * @param pattern A glob pattern for classes to be avoided from the parent classloader
   */
  public DelegateRelationshipBuilder addBlacklistedClassPattern(String pattern) {
    this.blacklistedClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  /**
   * Adds a glob pattern for classes to be allowed to be loaded from the parent classloader, if they don't exist in the
   * child classloader.
   *
   * @param pattern A glob pattern for classes to be allowed to be loaded from the parent classloader
   */
  public DelegateRelationshipBuilder addWhitelistedClassPattern(String pattern) {
    this.whitelistedClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  /**
   * Builds an instance of a {@link DelegateRelationship} with the given parameters.
   *
   * @return A {@link DelegateRelationship} with the given parameters.
   */
  public DelegateRelationship build() {
    return new DelegateRelationship(this.delegateClassLoader, this.isolationLevel, this.delegatePreferredClassPatterns,
        this.blacklistedClassPatterns, this.whitelistedClassPatterns);
  }
}

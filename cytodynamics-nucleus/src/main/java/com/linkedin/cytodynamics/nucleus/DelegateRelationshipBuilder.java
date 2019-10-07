/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import com.linkedin.cytodynamics.matcher.GlobMatcher;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;


/**
 * Delegate relationship builder fluent interface, used to build {@link DelegateRelationship} with a specific level of
 * isolation from a delegate {@link ClassLoader}.
 */
public final class DelegateRelationshipBuilder {
  private ClassLoader delegateClassLoader = getClass().getClassLoader();
  private IsolationLevel isolationLevel = IsolationLevel.NONE;
  private Set<Predicate<String>> delegatePreferredClassPredicates = new HashSet<>();
  private Set<Predicate<String>> blacklistedClassPredicates = new HashSet<>();
  private Set<Predicate<String>> whitelistedClassPredicates = new HashSet<>();

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
   * Sets the delegate classloader for this relationship. By default, this is the classloader which loaded the
   * {@link DelegateRelationshipBuilder} class.
   *
   * @param delegateClassLoader delegate classloader for the relationship
   */
  public DelegateRelationshipBuilder withDelegateClassLoader(ClassLoader delegateClassLoader) {
    this.delegateClassLoader = delegateClassLoader;
    return this;
  }

  /**
   * Sets the isolation level of the delegate relationship. See {@link IsolationLevel} for more details on isolation
   * levels.
   *
   * @param isolationLevel The isolation level of the delegate relationship.
   */
  public DelegateRelationshipBuilder withIsolationLevel(IsolationLevel isolationLevel) {
    this.isolationLevel = isolationLevel;
    return this;
  }

  /**
   * Adds a {@link Predicate} for class names to be loaded from the delegate classloader as opposed to the child
   * classloader.
   * This can be used to force the child to use certain classes which must have a common implementation, such as logging
   * libraries or bootstrap classes.
   *
   * @param predicate A {@link Predicate} for a class name to load from the delegate classloader, such as
   *                  "org.apache.log4j.*"
   */
  public DelegateRelationshipBuilder addDelegatePreferredClassPredicate(Predicate<String> predicate) {
    this.delegatePreferredClassPredicates.add(predicate);
    return this;
  }

  /**
   * Adds a {@link Predicate} for class names never to be loaded from the delegate classloader, even if they have an
   * <code>@Api</code> annotation.
   *
   * @param predicate A {@link Predicate} for a class name to be avoided from the delegate classloader
   */
  public DelegateRelationshipBuilder addBlacklistedClassPredicate(Predicate<String> predicate) {
    this.blacklistedClassPredicates.add(predicate);
    return this;
  }

  /**
   * Adds a {@link Predicate} for class names to be allowed to be loaded from the delegate classloader, if they don't
   * exist in the child classloader.
   *
   * @param predicate A {@link Predicate} for a class name to be allowed to be loaded from the delegate classloader
   */
  public DelegateRelationshipBuilder addWhitelistedClassPredicate(Predicate<String> predicate) {
    this.whitelistedClassPredicates.add(predicate);
    return this;
  }

  /**
   * Builds an instance of a {@link DelegateRelationship} with the given parameters.
   *
   * @return A {@link DelegateRelationship} with the given parameters.
   */
  public DelegateRelationship build() {
    return new DelegateRelationship(this.delegateClassLoader, this.isolationLevel, this.delegatePreferredClassPredicates,
        this.blacklistedClassPredicates, this.whitelistedClassPredicates);
  }
}

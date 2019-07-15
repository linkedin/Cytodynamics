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
import java.util.List;


/**
 * Loader builder fluent interface, used to build loaders with a given classpath and a specific level of isolation from
 * the parent's classloader.
 */
public final class LoaderBuilder {
  private final List<URI> classpath = new ArrayList<>();
  private OriginRestriction originRestriction = null;
  private final List<ParentRelationship> parentRelationships = new ArrayList<>();

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

  public LoaderBuilder addParentRelationship(ParentRelationship parentRelationship) {
    this.parentRelationships.add(parentRelationship);
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

    return new IsolatingLoader(classpath, originRestriction, parentRelationships);
  }
}

/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import com.linkedin.cytodynamics.exception.InvalidBuilderParametersException;
import com.linkedin.cytodynamics.exception.OriginValidationException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Loader builder fluent interface, used to build an isolated classloader.
 */
public final class LoaderBuilder {
  private final List<URI> classpath = new ArrayList<>();
  private OriginRestriction originRestriction = null;
  private DelegateRelationship parentRelationship = null;
  private final List<DelegateRelationship> fallbackDelegates = new ArrayList<>();

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

  /**
   * Set the delegate relationship for the loader. This will be checked first when trying to load a class.
   *
   * @param parentRelationship primary {@link DelegateRelationship} to use for the loader
   */
  public LoaderBuilder withParentRelationship(DelegateRelationship parentRelationship) {
    this.parentRelationship = parentRelationship;
    return this;
  }

  /**
   * Add a fallback delegate classloader for the loader. If an entity cannot be loaded through the primary delegate
   * relationship, then the loader will attempt to find it in the fallback(s).
   * The fallback delegates will be used in the order that they are added to this builder.
   *
   * @param fallbackDelegate a fallback {@link DelegateRelationship} to use for the loader
   */
  public LoaderBuilder addFallbackDelegate(DelegateRelationship fallbackDelegate) {
    this.fallbackDelegates.add(fallbackDelegate);
    return this;
  }

  /**
   * Builds an instance of a loader with the given parameters.
   *
   * @return A loader with the given parameters.
   */
  public ClassLoader build() {
    URL[] classpathUrls = validateAndGetClassPathUrls();
    if (this.parentRelationship == null) {
      throw new InvalidBuilderParametersException(
          "No parent relationship set; please use withParentRelationship() to set one");
    }
    return new IsolatingClassLoader(classpathUrls, this.parentRelationship, this.fallbackDelegates);
  }

  private URL[] validateAndGetClassPathUrls() {
    if (originRestriction == null) {
      throw new InvalidBuilderParametersException(
          "No origin restriction set, use OriginRestriction.allowByDefault() if no restriction is desired");
    }

    URL[] classpathUrls = new URL[classpath.size()];
    for (int i = 0; i < classpathUrls.length; i++) {
      URL url = toURL(classpath.get(i));
      if (!originRestriction.isAllowed(url)) {
        throw new OriginValidationException(
            "Loading classes from " + url + " is forbidden by the origin restriction. Aborting.");
      } else {
        classpathUrls[i] = url;
      }
    }
    return classpathUrls;
  }

  private static URL toURL(URI uri) {
    try {
      return uri.toURL();
    } catch (MalformedURLException e) {
      throw new InvalidBuilderParametersException("Unable to convert URI " + uri + " to a URL", e);
    }
  }
}

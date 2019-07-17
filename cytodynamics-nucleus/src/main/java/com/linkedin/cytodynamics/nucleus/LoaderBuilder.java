/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Loader builder fluent interface, used to build an isolated classloader with a given classpath and relationships to
 * the delegate classloader(s).
 */
public final class LoaderBuilder {
  private final List<URI> classpath = new ArrayList<>();
  private OriginRestriction originRestriction = null;
  private DelegateRelationship primaryDelegate = null;
  private final List<ClassLoader> fallbackDelegates = new ArrayList<>();

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
   * Set the primary delegate relationship for the loader. This will be checked first when trying to load a class.
   *
   * @param primaryDelegate primary {@link DelegateRelationship} to use for the loader
   */
  public LoaderBuilder withPrimaryDelegate(DelegateRelationship primaryDelegate) {
    this.primaryDelegate = primaryDelegate;
    return this;
  }

  /**
   * Add a fallback delegate classloader for the loader. If an entity cannot be loaded through the primary delegate
   * relationship, then the loader will attempt to find it in the fallback(s).
   * The fallback delegates will be used in the order that they are added to this builder.
   *
   * @param fallbackDelegate a fallback {@link DelegateRelationship} to use for the loader
   */
  public LoaderBuilder addFallbackDelegate(ClassLoader fallbackDelegate) {
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
    if (this.primaryDelegate == null) {
      throw new RuntimeException("No primary delegate set; please use withPrimaryDelegate() to set one");
    }
    return new IsolatingClassLoader(classpathUrls, this.primaryDelegate, this.fallbackDelegates);
  }

  private URL[] validateAndGetClassPathUrls() {
    if (originRestriction == null) {
      throw new RuntimeException("No origin restriction set, use OriginRestriction.allowByDefault() if no restriction is desired");
    }

    URL[] classpathUrls = new URL[classpath.size()];
    for (int i = 0; i < classpathUrls.length; i++) {
      try {
        URL url = classpath.get(i).toURL();

        if (!originRestriction.isAllowed(url)) {
          throw new SecurityException("Loading classes from " + url + " is forbidden by the origin restriction. Aborting.");
        } else {
          classpathUrls[i] = url;
        }
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return classpathUrls;
  }
}

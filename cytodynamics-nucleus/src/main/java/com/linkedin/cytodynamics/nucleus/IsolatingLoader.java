/**
 * Copyright 2018 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;


/**
 * Isolating loader which delegates most of its work to the IsolatingClassLoader.
 */
class IsolatingLoader implements Loader {
  private final IsolationLevel isolationLevel;
  private final List<URI> classpath;
  private final IsolatingClassLoader classloader;

  IsolatingLoader(List<URI> classpath, OriginRestriction originRestriction, IsolationLevel isolationLevel, Set<GlobMatcher> parentPreferredClassPatterns,
      Set<GlobMatcher> whitelistedClassPatterns, Set<GlobMatcher> blacklistedClassPatterns) {
    this.isolationLevel = isolationLevel;
    this.classpath = classpath;

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

    classloader = new IsolatingClassLoader(classpathUrls, getClass().getClassLoader(), isolationLevel,
        parentPreferredClassPatterns, whitelistedClassPatterns, blacklistedClassPatterns);
  }

  @Override
  public <T> T newInstanceOf(Class<T> type, String className) {
    try {
      return loadClass(type, className, false).newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> Class<? extends T> loadClass(Class<T> type, String className) {
    return loadClass(type, className, true);
  }

  private <T> Class<? extends T> loadClass(Class<T> type, String className, boolean swallowExceptions) {
    try {
      Class<? extends T> clazz = (Class<? extends T>) classloader.loadClass(className);
      return clazz;
    } catch (ClassNotFoundException e) {
      if (swallowExceptions) {
        return null;
      } else {
        throw new RuntimeException(e);
      }
    }
  }
}

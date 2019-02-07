/**
 * BSD 2-CLAUSE LICENSE
 *
 * Copyright 2018 LinkedIn Corporation.
 * All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

  IsolatingLoader(List<URI> classpath, IsolationLevel isolationLevel, Set<GlobMatcher> parentPreferredClassPatterns,
      Set<GlobMatcher> whitelistedClassPatterns, Set<GlobMatcher> blacklistedClassPatterns) {
    this.isolationLevel = isolationLevel;
    this.classpath = classpath;

    URL[] classpathUrls = new URL[classpath.size()];
    for (int i = 0; i < classpathUrls.length; i++) {
      try {
        classpathUrls[i] = classpath.get(i).toURL();
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

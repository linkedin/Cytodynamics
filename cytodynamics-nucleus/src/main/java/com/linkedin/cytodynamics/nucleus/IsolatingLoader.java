/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
      return loadClass(type, className).newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      return null;
    }
  }

  @Override
  public <T> Class<? extends T> loadClass(Class<T> type, String className) {
    try {
      Class<? extends T> clazz = (Class<? extends T>) classloader.loadClass(className);
      return clazz;
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}

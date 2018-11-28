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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
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

  private LoaderBuilder() {
  }

  public static LoaderBuilder anIsolatingLoader() {
    return new LoaderBuilder();
  }

  public LoaderBuilder withIsolationLevel(IsolationLevel isolationLevel) {
    this.isolationLevel = isolationLevel;
    return this;
  }

  public LoaderBuilder withClasspath(List<URI> classpath) {
    this.classpath.addAll(classpath);
    return this;
  }

  public LoaderBuilder addParentPreferredClassPattern(String pattern) {
    parentPreferredClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  public LoaderBuilder addBlacklistedClassPattern(String pattern) {
    blacklistedClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  public LoaderBuilder addWhitelistedClassPattern(String pattern) {
    whitelistedClassPatterns.add(new GlobMatcher(pattern));
    return this;
  }

  public Loader build() {
    return new IsolatingLoader(classpath, isolationLevel, parentPreferredClassPatterns, whitelistedClassPatterns, blacklistedClassPatterns);
  }
}

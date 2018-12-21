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

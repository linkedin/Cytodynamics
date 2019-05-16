package com.linkedin.cytodynamics.nucleus;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ChainedLoader implements Loader {
  private final List<Loader> loaders;

  public ChainedLoader(List<Loader> loaders) {
    this.loaders = loaders;
  }

  @Override
  public <T> T newInstanceOf(Class<T> type, String className) {
    for (Loader loader : this.loaders) {
      T newInstance = loader.newInstanceOf(type, className);
      // TODO still need to handle exceptions thrown by loader
      if (newInstance != null) {
        return newInstance;
      }
    }
    // TODO maybe throw an exception
    return null;
  }

  @Override
  public <T> Class<? extends T> loadClass(Class<T> type, String className) {
    for (Loader loader : this.loaders) {
      Class<? extends T> loadedClass = loader.loadClass(type, className);
      // TODO still need to handle exceptions thrown by loader
      if (loadedClass != null) {
        return loadedClass;
      }
    }
    // TODO maybe throw an exception
    return null;
  }

  private static class Example {
    public Example() {
      ClassLoader parent = getClass().getClassLoader();
      URL[] childClasspath = new URL[] {/* need to fill this in */};
      // set parent to null here so that this only loads classes on its direct classpath or the bootstrap classloader
      URLClassLoader child = new URLClassLoader(childClasspath, null);

      List<URI> firstIsolatingLoaderClasspath = new ArrayList<>(); // need to fill this in
      IsolatingLoader firstIsolatingLoader =
          new IsolatingLoader(firstIsolatingLoaderClasspath, OriginRestriction.allowByDefault(), IsolationLevel.FULL,
              Collections.emptySet(), Collections.emptySet(), Collections.emptySet());

      List<URI> secondIsolatingLoaderClasspath = new ArrayList<>(); // need to fill this in
      IsolatingLoader secondIsolatingLoader =
          new IsolatingLoader(secondIsolatingLoaderClasspath, OriginRestriction.allowByDefault(), IsolationLevel.NONE,
              Collections.emptySet(), Collections.emptySet(), Collections.emptySet());

      Loader finalLoader = new ChainedLoader(Arrays.asList(firstIsolatingLoader, secondIsolatingLoader));
    }
  }
}

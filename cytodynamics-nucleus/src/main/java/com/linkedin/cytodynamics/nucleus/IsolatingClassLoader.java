/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.function.BiFunction;


/**
 * Isolating classloader, used to separate classes.
 */
class IsolatingClassLoader extends URLClassLoader {
  private static final Logger LOGGER = LogApiAdapter.getLogger(IsolatingClassLoader.class);

  private final List<ParentClassLoaderContext> parentClassLoaderContexts;

  /**
   * @param classpath classpath for this classloader
   * @param parentClassLoaderContexts non-empty list of {@link ParentClassLoaderContext}s
   */
  IsolatingClassLoader(URL[] classpath, List<ParentClassLoaderContext> parentClassLoaderContexts) {
    super(classpath, validate(parentClassLoaderContexts).get(0).getParentClassLoader());
    this.parentClassLoaderContexts = parentClassLoaderContexts;
  }

  /**
   * Validates that {@code parentClassLoaderContexts} is non-empty.
   */
  private static List<ParentClassLoaderContext> validate(List<ParentClassLoaderContext> parentClassLoaderContexts) {
    if (parentClassLoaderContexts.isEmpty()) {
      throw new IllegalArgumentException("Must have at least one parent");
    }
    return parentClassLoaderContexts;
  }

  enum IsolationBehaviors {
    USE_CHILD_CLASS((parent, child) -> child),
    USE_PARENT_CLASS((parent, child) -> parent),
    USE_PARENT_AND_LOG((parent, child) -> {
      LOGGER.warn("Class " + parent.getName() + " used from the parent classloader would not be visible when running under full isolation.");
      return parent;
    }),
    CLASS_NOT_FOUND((parent, child) -> null);

    IsolationBehaviors(BiFunction<Class<?>, Class<?>, Class<?>> function) {
      _function = function;
    }

    private final BiFunction<Class<?>, Class<?>, Class<?>> _function;

    public Class<?> getEffectiveClass(Class<?> parent, Class<?> child) {
      return _function.apply(parent, child);
    }
  }

  private static final IsolationBehaviors[][] ISOLATION_BEHAVIORS = new IsolationBehaviors[][]{
      // NONE
      {
          IsolationBehaviors.CLASS_NOT_FOUND,     // Not in parent or child
          IsolationBehaviors.USE_CHILD_CLASS,     // Only in child
          IsolationBehaviors.USE_PARENT_CLASS,    // Only in parent
          IsolationBehaviors.USE_CHILD_CLASS      // In parent and child
      },
      // TRANSITIONAL
      {
          IsolationBehaviors.CLASS_NOT_FOUND,     // Not in parent or child
          IsolationBehaviors.USE_CHILD_CLASS,     // Only in child
          IsolationBehaviors.USE_PARENT_AND_LOG,  // Only in parent
          IsolationBehaviors.USE_CHILD_CLASS      // In parent and child
      },
      // FULL
      {
          IsolationBehaviors.CLASS_NOT_FOUND,     // Not in parent or child
          IsolationBehaviors.USE_CHILD_CLASS,     // Only in child
          IsolationBehaviors.CLASS_NOT_FOUND,     // Only in parent
          IsolationBehaviors.USE_CHILD_CLASS      // In parent and child
      }
  };

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    for (ParentClassLoaderContext parentClassLoaderContext : this.parentClassLoaderContexts) {
      Class<?> loadedClass = tryLoadClassWithParent(name, parentClassLoaderContext);
      if (loadedClass != null) {
        return loadedClass;
      }
    }
    // got through all parents but could not find any class
    throw new ClassNotFoundException("Could not load class for name " + name);
  }

  /**
   * Try to load a class corresponding to an individual {@link ParentClassLoaderContext}.
   *
   * @param name name of the class to load
   * @param parentClassLoaderContext {@link ParentClassLoaderContext} to use for loading
   * @return {@link Class} corresponding to {@code name} if a class could be resolved corresponding to the
   * {@code parentClassLoaderContext}; null otherwise
   */
  private Class<?> tryLoadClassWithParent(String name, ParentClassLoaderContext parentClassLoaderContext) {
    Class<?> parentClass = null;
    Class<?> childClass = null;

    // Is the class blacklisted from being loaded from the parent?
    boolean isBlacklisted = false;
    for (GlobMatcher blacklistedClassPattern : parentClassLoaderContext.getBlacklistedClassPatterns()) {
      if (blacklistedClassPattern.matches(name)) {
        isBlacklisted = true;
        break;
      }
    }

    // Is it already loaded in the parent class loader?
    try {
      if (!isBlacklisted) {
        parentClass = parentClassLoaderContext.getParentClassLoader().loadClass(name);

        // Is it part of the exported API or part of core Java?
        if (parentClass.isAnnotationPresent(Api.class)) {
          return parentClass;
        } else {
          // Is it parent preferred?
          for (GlobMatcher parentPreferredClassPattern : parentClassLoaderContext.getParentPreferredClassPatterns()) {
            if (parentPreferredClassPattern.matches(name)) {
              return parentClass;
            }
          }
        }
      }
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      // It doesn't exist in the parent class loader, try to load it.
    }

    try {
      childClass = findClass(name);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      // Ignored
    }

    Class<?> returnValue =
        getIsolationBehavior(parentClassLoaderContext.getIsolationLevel(), parentClass, childClass).getEffectiveClass(
            parentClass, childClass);

    // Is it whitelisted and present in the parent class loader but hidden due to the isolation behavior?
    if (returnValue == null && parentClass != null) {
      for (GlobMatcher whitelistedClassPattern : parentClassLoaderContext.getWhitelistedClassPatterns()) {
        if (whitelistedClassPattern.matches(name)) {
          return parentClass;
        }
      }
    }

    return returnValue;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    return super.findClass(name);
  }

  private static IsolationBehaviors getIsolationBehavior(IsolationLevel isolationLevel, Class<?> parentClass,
      Class<?> childClass) {
    boolean hasParentClass = parentClass != null;
    boolean hasChildClass = childClass != null;
    int behaviorIndex = (hasParentClass ? 0b10 : 0b00) | (hasChildClass ? 0b01 : 0b00);

    return ISOLATION_BEHAVIORS[isolationLevel.ordinal()][behaviorIndex];
  }
}

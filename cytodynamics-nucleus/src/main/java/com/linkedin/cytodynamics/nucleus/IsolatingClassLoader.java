/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.linkedin.cytodynamics.exception.CytodynamicsClassNotFoundException;
import com.linkedin.cytodynamics.isolation.Chooser;
import com.linkedin.cytodynamics.isolation.ChooserMappingFactory;


/**
 * Isolating classloader, used to separate classes.
 */
class IsolatingClassLoader extends URLClassLoader {
  private static final Logger LOGGER = LogApiAdapter.getLogger(IsolatingClassLoader.class);
  private static final Map<IsolationLevel, Chooser<Class<?>>> CLASS_CHOOSER_MAPPING =
      ChooserMappingFactory.buildChooserMapping(delegate -> LOGGER.warn(String.format(
          "Class %s used from the delegate classloader would not be visible if running under FULL isolation, unless "
              + "whitelisting is used.",
          delegate.getName())));
  private static final Map<IsolationLevel, Chooser<URL>> RESOURCE_CHOOSER_MAPPING =
      ChooserMappingFactory.buildChooserMapping(delegate -> LOGGER.warn(String.format(
          "Resource %s used from the delegate classloader would not be visible if running under FULL isolation, unless "
              + "whitelisting is used.",
          delegate.toString())));
  private static final Map<IsolationLevel, Chooser<List<URL>>> RESOURCES_CHOOSER_MAPPING =
      ChooserMappingFactory.buildChooserMappingForList(delegate -> LOGGER.warn(String.format(
          "Resources [%s] used from the delegate classloader would not be visible if running under FULL isolation, "
              + "unless whitelisting is used.",
          delegate.stream().map(URL::toString).collect(Collectors.joining(",")))));

  private final DelegateRelationship parentRelationship;
  private final List<DelegateRelationship> fallbackDelegates;

  /**
   * @param classpath classpath for this classloader
   * @param parentRelationship non-null primary {@link DelegateRelationship}
   * @param fallbackDelegates list of fallback {@link ClassLoader}s; may be empty, but must be non-null
   */
  IsolatingClassLoader(URL[] classpath, DelegateRelationship parentRelationship,
      List<DelegateRelationship> fallbackDelegates) {
    /*
     * Use the classloader from the parent relationship as the parent classloader, since that will be checked first when
     * loading a class.
     */
    super(classpath, parentRelationship.getDelegateClassLoader());
    this.parentRelationship = parentRelationship;
    this.fallbackDelegates = fallbackDelegates;
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
      // check if the class has already been loaded
      Class<?> cl = findLoadedClass(name);

      if (cl == null) {
        // try to load the class using the parent
        cl = tryLoadClassWithDelegate(name, this.parentRelationship);
      }

      if (cl == null) {
        // try to load the class using a fallback
        for (DelegateRelationship fallbackDelegate : this.fallbackDelegates) {
          cl = tryLoadClassWithDelegate(name, fallbackDelegate);
          if (cl != null) {
            break;
          }
        }
      }

      if (cl != null) {
        if (resolve) {
          doResolveClass(cl);
        }
        return cl;
      } else {
        // got through parent and fallback delegates but could not find the class
        throw new CytodynamicsClassNotFoundException(String.format(
            "Could not fully load class for name %s. It is possible that the immediate class is found, but a class that "
                + "it depends on cannot be found", name));
      }
    }
  }

  @Override
  public URL getResource(String name) {
    URL resource = tryLoadResourceWithDelegate(name, this.parentRelationship);
    if (resource != null) {
      return resource;
    }
    for (DelegateRelationship fallbackDelegate : this.fallbackDelegates) {
      resource = tryLoadResourceWithDelegate(name, fallbackDelegate);
      if (resource != null) {
        return resource;
      }
    }
    // could not find resource anywhere
    return null;
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    /*
     * Using a LinkedHashSet for ordering (search for resources in a certain order) and uniqueness (might return
     * same resource from multiple search paths).
     * Search through both the parent and the fallbacks for resources.
     */
    LinkedHashSet<URL> resources = new LinkedHashSet<>(loadResourcesWithDelegate(name, this.parentRelationship));
    for (DelegateRelationship fallbackDelegate : this.fallbackDelegates) {
      resources.addAll(loadResourcesWithDelegate(name, fallbackDelegate));
    }
    return Collections.enumeration(resources);
  }

  /*
   * It is currently unnecessary to override getResourceAsStream, since it uses getResource to get a resource, and
   * getResource is overridden by this class.
   */

  /**
   * Try to load a class corresponding to an individual {@link DelegateRelationship}.
   *
   * @param name name of the class to load
   * @param delegateRelationship {@link DelegateRelationship} to use for loading
   * @return {@link Class} corresponding to {@code name} if a class could be resolved corresponding to the
   * {@code delegateRelationship}; null otherwise
   */
  private Class<?> tryLoadClassWithDelegate(String name, DelegateRelationship delegateRelationship) {
    Class<?> delegateClass = null;
    // class might be blacklisted from being loaded from the delegate
    boolean isBlacklisted = matchesPredicate(delegateRelationship.getBlacklistedClassPredicates(), name);
    if (!isBlacklisted) {
      delegateClass = tryLoadClass(delegateRelationship.getDelegateClassLoader(), name);
      // delegateClass might still be null; just move to next section if it is still null
      if (delegateClass != null) {
        /*
         * Is the class part of the exported API?
         *
         * Note: We need to load the Api class from the same classloader which loaded the delegateClass. If we just used
         * Api.class directly, then that would come from the classloader which loaded this IsolatingClassLoader. That
         * classloader might be different than the delegate classloader, so they would each load a different instance of
         * the Api class, and they would not be considered the same class for the purposes of the isAnnotationPresent
         * method.
         */
        // noinspection unchecked: safe to cast since Api is an annotation class
        Class<? extends Annotation> apiAnnotationClass =
            (Class<? extends Annotation>) tryLoadClass(delegateRelationship.getDelegateClassLoader(),
                Api.class.getName());
        if (apiAnnotationClass != null && delegateClass.isAnnotationPresent(apiAnnotationClass)) {
          // class is part of exported API
          return delegateClass;
        } else if (matchesPredicate(delegateRelationship.getDelegatePreferredClassPredicates(), name)) {
          // class is delegate-preferred
          return delegateClass;
        }
      }
    }

    Class<?> childClass = null;
    try {
      childClass = findClass(name);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      // still ok so far; still might be able to use the class from delegate
    }

    Class<?> returnValue =
        CLASS_CHOOSER_MAPPING.get(delegateRelationship.getIsolationLevel()).choose(delegateClass, childClass);

    // Is it whitelisted and present in the delegate class loader but hidden due to the isolation behavior?
    if (returnValue == null && delegateClass != null) {
      if (matchesPredicate(delegateRelationship.getWhitelistedClassPredicates(), name)) {
        return delegateClass;
      }
    }

    return returnValue;
  }

  /**
   * Try to load a resource corresponding to an individual {@link DelegateRelationship}.
   *
   * @param name name of the resource to load
   * @param delegateRelationship {@link DelegateRelationship} to use for loading
   * @return {@link URL} for resource corresponding to {@code name} if a resource could be resolved corresponding to the
   * {@code delegateRelationship}; null otherwise
   */
  private URL tryLoadResourceWithDelegate(String name, DelegateRelationship delegateRelationship) {
    URL delegateResource = null;
    // resource might be blacklisted from being loaded from the delegate
    boolean isBlacklisted = matchesPredicate(delegateRelationship.getBlacklistedResourcePredicates(), name);
    if (!isBlacklisted) {
      delegateResource = delegateRelationship.getDelegateClassLoader().getResource(name);
      // delegateResource might still be null; just move to next section if it is still null
      if (delegateResource != null) {
        if (matchesPredicate(delegateRelationship.getDelegatePreferredResourcePredicates(), name)) {
          // resource is delegate-preferred
          return delegateResource;
        }
      }
    }

    URL childResource = findResource(name);

    URL returnValue =
        RESOURCE_CHOOSER_MAPPING.get(delegateRelationship.getIsolationLevel()).choose(delegateResource, childResource);

    // Is it whitelisted and present in the delegate class loader but hidden due to the isolation behavior?
    if (returnValue == null && delegateResource != null) {
      if (matchesPredicate(delegateRelationship.getWhitelistedResourcePredicates(), name)) {
        return delegateResource;
      }
    }

    return returnValue;
  }

  /**
   * Load resources using a certain {@code delegateRelationship}. This will merge resources from the delegate and/or
   * child based on isolation level and whitelists.
   */
  private List<URL> loadResourcesWithDelegate(String name, DelegateRelationship delegateRelationship)
      throws IOException {
    /*
     * Using a LinkedHashSet for ordering (search for resources in a certain order) and uniqueness (might return
     * same resource from multiple search paths).
     */
    LinkedHashSet<URL> resources = new LinkedHashSet<>();
    List<URL> delegateResources = Collections.emptyList();
    // resource might be blacklisted from being loaded from the delegate
    boolean isBlacklisted = matchesPredicate(delegateRelationship.getBlacklistedResourcePredicates(), name);
    if (!isBlacklisted) {
      delegateResources = Collections.list(delegateRelationship.getDelegateClassLoader().getResources(name));
      if (matchesPredicate(delegateRelationship.getDelegatePreferredResourcePredicates(), name)) {
        // resources are delegate-preferred, so add delegate resources first
        resources.addAll(delegateResources);
      }
    }

    List<URL> childResources = Collections.list(findResources(name));
    List<URL> chosenResources = chooseResources(delegateRelationship, delegateResources, childResources);
    resources.addAll(chosenResources);
    if (matchesPredicate(delegateRelationship.getWhitelistedResourcePredicates(), name)) {
      resources.addAll(delegateResources);
    }

    return new ArrayList<>(resources);
  }

  /**
   * Calls the isolation chooser to choose resources. Assumes delegateResources and childResources are non-null.
   */
  private static List<URL> chooseResources(DelegateRelationship delegateRelationship, List<URL> delegateResources,
      List<URL> childResources) {
    // an empty list means no resources are found, and need to use null for chooser to denote "not found"
    List<URL> delegateResourcesOrNull = delegateResources.isEmpty() ? null : delegateResources;
    List<URL> childResourcesOrNull = childResources.isEmpty() ? null : childResources;
    List<URL> chosenResourcesOrNull = RESOURCES_CHOOSER_MAPPING.get(delegateRelationship.getIsolationLevel())
        .choose(delegateResourcesOrNull, childResourcesOrNull);
    return chosenResourcesOrNull == null ? Collections.emptyList() : chosenResourcesOrNull;
  }

  private static boolean matchesPredicate(Set<Predicate<String>> predicates, String value) {
    return predicates.stream().anyMatch(predicate -> predicate.test(value));
  }

  private static Class<?> tryLoadClass(ClassLoader classLoader, String name) {
    try {
      return classLoader.loadClass(name);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      return null;
    }
  }

  /**
   * Package-private for testing purposes, since resolveClass is protected within {@link ClassLoader}.
   */
  void doResolveClass(Class<?> cl) {
    resolveClass(cl);
  }
}

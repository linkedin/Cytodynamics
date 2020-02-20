/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.test;

import com.linkedin.cytodynamics.exception.CytodynamicsClassNotFoundException;
import com.linkedin.cytodynamics.exception.OriginValidationException;
import com.linkedin.cytodynamics.matcher.BootstrapClassPredicate;
import com.linkedin.cytodynamics.matcher.GlobMatcher;
import com.linkedin.cytodynamics.nucleus.IsolationLevel;
import com.linkedin.cytodynamics.nucleus.LoaderBuilder;
import com.linkedin.cytodynamics.nucleus.OriginRestriction;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.linkedin.cytodynamics.nucleus.DelegateRelationshipBuilder;
import org.junit.Ignore;
import org.junit.Test;

import static com.linkedin.cytodynamics.util.JarUtil.*;
import static org.junit.Assert.*;


/**
 * TODO: Convert some tests into unit tests which mock out the actual classloading, so we don't need to deal with so
 * many cases that need actual JARs to be set up
 */
public class TestDynamicLoad {
  private static final String DATA_TXT_RESOURCE_NAME = "data.txt";
  private static final String API_ONLY_TXT_RESOURCE_NAME = "api-only.txt";

  @Test
  public void testStaticLoad() {
    TestInterface implementation = new TestInterfaceImpl();
    String value = implementation.getValue();

    // Can be either A or B
    if (!value.equals("A") && !value.endsWith("B")) {
      fail();
    }
  }

  @Test
  public void testLoadA() throws Exception {
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPredicate(new GlobMatcher("java.*"))
            .addWhitelistedClassPredicate(new GlobMatcher("com.intellij.*"))
            .build())
        .build();

    Class<?> testInterfaceImplClass = loader.loadClass(TestInterfaceImpl.class.getName());
    // try to load the class again, should return the same class
    assertEquals(loader.loadClass(TestInterfaceImpl.class.getName()), testInterfaceImplClass);
    TestInterface implementation = (TestInterface) testInterfaceImplClass.newInstance();
    assertEquals(implementation.getValue(), "A");
    // try to load the class again after loading an instance
    assertEquals(loader.loadClass(TestInterfaceImpl.class.getName()), testInterfaceImplClass);
    // load a different class
    implementation = (TestInterface) loader.loadClass(TestInterfaceAOnlyImpl.class.getName()).newInstance();
    assertEquals(implementation.getValue(), "A-only");
  }

  @Test
  public void testLoadResourcesFullIsolation() throws IOException {
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-api").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.FULL)
            .build())
        .build();
    // should not find parent resource
    assertResourcesFound(loader, DATA_TXT_RESOURCE_NAME, Collections.singletonList("A"));
  }

  @Test
  public void testLoadResourcesFullIsolationNoneFound() throws IOException {
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-api").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.FULL)
            .build())
        .build();
    assertNull(loader.getResource(API_ONLY_TXT_RESOURCE_NAME));
    assertNull(loader.getResourceAsStream(API_ONLY_TXT_RESOURCE_NAME));
    assertFalse(loader.getResources(API_ONLY_TXT_RESOURCE_NAME).hasMoreElements());
  }

  @Test
  public void testLoadResourcesNoneIsolation() throws IOException {
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-api").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.NONE)
            .build())
        .build();
    // should include parent resource after own resource
    assertResourcesFound(loader, DATA_TXT_RESOURCE_NAME, Arrays.asList("A", "API"));
  }

  @Test
  public void testLoadResourcesNoneIsolationNoneFound() throws IOException {
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-api").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.NONE)
            .build())
        .build();
    assertNull(loader.getResource("not-a-resource-file.txt"));
    assertNull(loader.getResourceAsStream("not-a-resource-file.txt"));
    assertFalse(loader.getResources("not-a-resource-file.txt").hasMoreElements());
  }

  @Test
  public void testLoadResourcesNoneIsolationOnlyInParent() throws IOException {
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-api").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.NONE)
            .build())
        .build();
    // should include parent resource after own resource
    assertResourcesFound(loader, API_ONLY_TXT_RESOURCE_NAME, Collections.singletonList("API-only"));
  }

  @Test
  public void testLoadResourcesWithDelegatePreferredFullIsolation() throws IOException {
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-api").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredResourcePredicate(new GlobMatcher(DATA_TXT_RESOURCE_NAME))
            .build())
        .build();

    // should prefer parent resource
    assertResourcesFound(loader, DATA_TXT_RESOURCE_NAME, Arrays.asList("API", "A"));
  }

  @Test
  public void testLoadResourcesWithDelegatePreferredFullIsolationOnlyInParent() throws IOException {
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-api").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredResourcePredicate(new GlobMatcher(API_ONLY_TXT_RESOURCE_NAME))
            .build())
        .build();

    // should prefer parent resource
    assertResourcesFound(loader, API_ONLY_TXT_RESOURCE_NAME, Collections.singletonList("API-only"));
  }

  @Test
  public void testLoadResourcesWithDelegatePreferredNoneIsolation() throws IOException {
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-api").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredResourcePredicate(new GlobMatcher(DATA_TXT_RESOURCE_NAME))
            .build())
        .build();

    // should prefer parent resource
    assertResourcesFound(loader, DATA_TXT_RESOURCE_NAME, Arrays.asList("API", "A"));
  }

  @Test
  public void testLoadResourcesWithDelegatePreferredNoneIsolationOnlyInParent() throws IOException {
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-api").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredResourcePredicate(new GlobMatcher(API_ONLY_TXT_RESOURCE_NAME))
            .build())
        .build();

    // should prefer parent resource
    assertResourcesFound(loader, API_ONLY_TXT_RESOURCE_NAME, Collections.singletonList("API-only"));
  }

  @Test
  public void testLoadResourcesWithWhitelist() throws IOException {
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-api").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedResourcePredicate(new GlobMatcher(API_ONLY_TXT_RESOURCE_NAME))
            .build())
        .build();
    assertResourcesFound(loader, API_ONLY_TXT_RESOURCE_NAME, Collections.singletonList("API-only"));
  }

  @Test
  public void testLoadResourcesWithFallback() throws IOException {
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-api").toURL()}, null);
    ClassLoader fallbackClassLoaderA = new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-b")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.NONE)
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderA)
            .withIsolationLevel(IsolationLevel.NONE)
            .build())
        .build();
    // prefer own resource, but parent and fallback resources are still visible
    assertResourcesFound(loader, DATA_TXT_RESOURCE_NAME, Arrays.asList("B", "API", "A"));
  }

  @Test
  public void testLoadB() throws Exception {
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-b")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPredicate(new GlobMatcher("java.*"))
            .addWhitelistedClassPredicate(new GlobMatcher("com.intellij.*"))
            .build())
        .build();

    TestInterface implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertEquals(implementation.getValue(), "B");
  }

  /**
   * Given that the loader is a parent of another classloader, then it should still properly do classloading.
   */
  @Test
  public void testLoadAsParent() throws Exception {
    URI apiJarUri = getTestJarUri("cytodynamics-test-api");
    // need cytodynamics-nucleus for Api annotation in parent
    URL cytodynamics = getNucleusUri().toURL();
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{cytodynamics, apiJarUri.toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Arrays.asList(apiJarUri, getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .build();

    // build a URLClassLoader with no classpath so only the parent would be used
    ClassLoader mainClassLoader = new URLClassLoader(new URL[]{}, loader);
    Class<?> testInterfaceClass = mainClassLoader.loadClass(TestInterface.class.getName());
    assertEquals("Should delegate up to the API classloader for the API class", testInterfaceClass.getClassLoader(),
        apiClassLoader);
    Class<?> nonApiTestInterface = mainClassLoader.loadClass(NonApiTestInterface.class.getName());
    assertEquals("If there is a non-API class which is also in API classloader, shouldn't load from API classloader",
        nonApiTestInterface.getClassLoader(), loader);
    Class<?> testInterfaceImplClass = mainClassLoader.loadClass(TestInterfaceImpl.class.getName());
    assertEquals("Should delegate up to the isolating loader for the concrete (non-API) class",
        testInterfaceImplClass.getClassLoader(), loader);
  }

  @Test
  public void testIsolation() throws Exception {
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPredicate(new GlobMatcher("java.*"))
            .addWhitelistedClassPredicate(new GlobMatcher("com.intellij.*"))
            .build())
        .build();

    TestInterface implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists(this.getClass().getName()));
  }

  @Test
  public void testIsolationLevels() throws Exception {
    // Full isolation
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPredicate(new GlobMatcher("java.lang.*"))
            .addWhitelistedClassPredicate(new GlobMatcher("com.intellij.*"))
            .build())
        .build();

    TestInterface implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.TRANSITIONAL)
            .build())
        .build();

    implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertTrue(implementation.classExists("java.lang.String"));
    assertTrue(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.NONE)
            .build())
        .build();

    implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertTrue(implementation.classExists("java.lang.String"));
    assertTrue(implementation.classExists("java.util.Set"));
  }

  @Test
  public void testBlacklist() throws Exception {
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPredicate(new GlobMatcher("java.*"))
            .addBlacklistedClassPredicate(new GlobMatcher("java.util.Set"))
            .addWhitelistedClassPredicate(new GlobMatcher("com.intellij.*"))
            .build())
        .build();

    TestInterface implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.TRANSITIONAL)
            .addBlacklistedClassPredicate(new GlobMatcher("java.util.*"))
            .build())
        .build();

    implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.NONE)
            .addBlacklistedClassPredicate(new GlobMatcher("java.util.Set"))
            .build())
        .build();

    implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));
  }

  @Test
  public void testLoadAandB() throws Exception {
    ClassLoader loaderA = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPredicate(new GlobMatcher("java.*"))
            .addWhitelistedClassPredicate(new GlobMatcher("com.intellij.*"))
            .build())
        .build();

    ClassLoader loaderB = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-b")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPredicate(new GlobMatcher("java.*"))
            .addWhitelistedClassPredicate(new GlobMatcher("com.intellij.*"))
            .build())
        .build();

    TestInterface implementationA = (TestInterface) loaderA.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    TestInterface implementationB = (TestInterface) loaderB.loadClass(TestInterfaceImpl.class.getName()).newInstance();

    assertEquals(implementationA.getValue(), "A");
    assertEquals(implementationB.getValue(), "B");
  }

  /**
   * Given that there is a parent and a fallback delegate, and all can load a class, the class should be loaded from the
   * parent.
   */
  @Test
  public void testLoadFromParentNotFallback() throws Exception {
    URL testApiJarURL = getTestJarUri("cytodynamics-test-api").toURL();
    ClassLoader parentClassLoaderA =
        new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader fallbackClassLoaderB =
        new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("cytodynamics-test-b").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        // will load class from parent, so don't need any classpath
        .withClasspath(Collections.emptyList())
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(parentClassLoaderA)
            // using NONE so that we can load the class from the parent
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderB)
            // NONE so that the class could potentially be loaded from here (shouldn't actually be loaded from here)
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .build();
    Class<?> clazz = loader.loadClass(TestInterfaceImpl.class.getName());
    assertEquals(clazz.getClassLoader(), parentClassLoaderA);
  }

  /**
   * Given that there is a parent and a fallback delegate, but only the fallback parent has a class, the class should be
   * loaded by the fallback.
   */
  @Test
  public void testLoadFromFallback() throws Exception {
    URL testApiJarURL = getTestJarUri("cytodynamics-test-api").toURL();
    ClassLoader parentClassLoaderA =
        new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader fallbackClassLoaderA =
        new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader fallbackClassLoaderB =
        new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("cytodynamics-test-b").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        // will load class from parent, so don't need any classpath
        .withClasspath(Collections.emptyList())
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(parentClassLoaderA)
            // using FULL so that the implementation class is not loaded from the parent
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderB)
            // using NONE so that the class is loaded from here
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderA)
            // NONE so that the class could potentially be loaded from here (shouldn't actually be loaded from here)
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .build();
    Class<?> clazz = loader.loadClass(TestInterfaceImpl.class.getName());
    assertEquals(clazz.getClassLoader(), fallbackClassLoaderB);
  }

  /**
   * Given that there is a parent and a fallback delegate, but only the final fallback has a class, the class should be
   * loaded by that final fallback.
   */
  @Test
  public void testLoadFromFinalFallback() throws Exception {
    URL testApiJarURL = getTestJarUri("cytodynamics-test-api").toURL();
    ClassLoader parentClassLoaderB =
        new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("cytodynamics-test-b").toURL()}, null);
    ClassLoader fallbackClassLoaderB =
        new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("cytodynamics-test-b").toURL()}, null);
    ClassLoader fallbackClassLoaderA =
        new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        // will load class from a delegate, so don't need any classpath
        .withClasspath(Collections.emptyList())
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(parentClassLoaderB)
            // using FULL so that the implementation class is not loaded from the parent
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderB)
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderA)
            // NONE so class can be loaded form here
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .build();
    Class<?> clazz = loader.loadClass(TestInterfaceAOnlyImpl.class.getName());
    assertEquals(clazz.getClassLoader(), fallbackClassLoaderA);
  }

  /**
   * Given FULL isolation level set for the parent and fallback, and a class is not an API, an exception should be
   * thrown.
   * This also tests that a {@link CytodynamicsClassNotFoundException} is thrown when a class can't be found.
   */
  @Test(expected = CytodynamicsClassNotFoundException.class)
  public void testIsolationForFallback() throws Exception {
    URL testApiJarURL = getTestJarUri("cytodynamics-test-api").toURL();
    ClassLoader parentClassLoaderA =
        new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader fallbackClassLoaderA =
        new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.emptyList())
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(parentClassLoaderA)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderA)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .build();
    loader.loadClass(TestInterfaceImpl.class.getName());
  }

  /**
   * Given a structure in which the classloader relationships form a graph, and two of the loaders have a common parent,
   * then delegation should properly load from the correct loaders.
   *
   * Graph structure:
   *
   * (common parent  <------------ (partial delegation
   *  classloader)                  classloader)
   *          ^                     ^
   *          |                    /
   *          |                   /
   *         FULL               NONE
   *          |                 /
   *          |                /
   *         (child classloader)
   *
   * In this structure, the implementation classes should come from the child or partial delegation classloaders, and
   * the common interface API class should come from the common parent.
   *
   * A more concrete use case for this is that the common parent would contain some shared API interfaces that need to
   * be used by both the partial delegation classloader and the child classloader. The partial delegation classloader
   * and the child classloader could be managed by different owners, so they would need to be separate and isolated.
   *
   * It should not be possible to build a cyclic graph due to the immutability of the cytodynamics loaders once they are
   * constructed. For example, if we wanted to create a loop between loader A and loader B, then B would need to exist
   * to construct A, and A would need to exist to construct B. We can't have both (excluding reflection). Therefore, we
   * are just considering an acyclic graph here.
   */
  @Test
  public void testGraphRelationshipWithCommonParent() throws Exception {
    URL apiJarUrl = getTestJarUri("cytodynamics-test-api").toURL();
    // need cytodynamics-nucleus for Api annotation in parent
    URL cytodynamics = getNucleusUri().toURL();
    ClassLoader commonParent = new URLClassLoader(new URL[]{cytodynamics, apiJarUrl}, null);
    ClassLoader partialDelegation =
        new URLClassLoader(new URL[]{getTestJarUri("cytodynamics-test-a").toURL()}, commonParent);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-b")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(commonParent)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(partialDelegation)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            // only load concrete classes from fallback; don't load API classes from fallback
            .addDelegatePreferredClassPredicate(new GlobMatcher(TestInterfaceImpl.class.getName()))
            .addDelegatePreferredClassPredicate(new GlobMatcher(TestInterfaceAOnlyImpl.class.getName()))
            .addBlacklistedClassPredicate(new GlobMatcher(TestInterface.class.getName()))
            .build())
        .build();

    Object testInterfaceImpl = loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertEquals("TestInterfaceImpl needs to come from the main loader", testInterfaceImpl.getClass().getClassLoader(),
        loader);
    Class<?> testInterfaceImplInterface = findTestInterface(testInterfaceImpl.getClass());
    assertEquals("TestInterface which is implemented by TestInterfaceImpl needs to come from the common parent",
        testInterfaceImplInterface.getClassLoader(), commonParent);
    Object testInterfaceAOnlyImpl = loader.loadClass(TestInterfaceAOnlyImpl.class.getName()).newInstance();
    assertEquals("TestInterfaceAOnlyImpl needs to come from the partial delegation classpath",
        testInterfaceAOnlyImpl.getClass().getClassLoader(), partialDelegation);
    Class<?> testInterfaceAOnlyImplInterface = findTestInterface(testInterfaceAOnlyImpl.getClass());
    assertEquals(
        "TestInterfaceAOnlyImpl which is implemented by TestInterfaceImpl needs to come from the common parent",
        testInterfaceAOnlyImplInterface.getClassLoader(), commonParent);
    // since these are both loaded from the common parent, then this should always be true, but double checking anyways
    assertEquals("TestInterface Class should be the same for TestInterfaceImpl and TestInterfaceAOnlyImpl",
        testInterfaceImplInterface, testInterfaceAOnlyImplInterface);
  }

  @Test
  public void testParentPreferred() throws Exception {
    TestInterface implementation = new TestInterfaceImpl();
    String parentValue = implementation.getValue();

    String jarToUse;
    if (parentValue.equals("A")) {
      jarToUse = "cytodynamics-test-b";
    } else {
      jarToUse = "cytodynamics-test-a";
    }

    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri(jarToUse)))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPredicate(new GlobMatcher("java.*"))
            .addWhitelistedClassPredicate(new GlobMatcher("com.intellij.*"))
            .addDelegatePreferredClassPredicate(new GlobMatcher("com.linkedin.cytodynamics.*"))
            .build())
        .build();

    TestInterface childImplementation =
        (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertEquals(childImplementation.getValue(), implementation.getValue());
    assertEquals(childImplementation.getClass(), implementation.getClass());
  }

  @Test
  public void testParentPreferredBootstrapClass() throws Exception {
    ClassLoader loader = LoaderBuilder.anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPredicate(new BootstrapClassPredicate())
            .build())
        .build();

    TestInterface implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists(this.getClass().getName()));
  }

  @Test
  public void testSecurity() throws Exception {
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    Path sourcePath = new File(getTestJarUri("cytodynamics-test-a")).toPath();
    File destinationFile = new File(tempDir, "a.jar");

    if (destinationFile.exists()) {
      destinationFile.delete();
    }

    Files.copy(sourcePath, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

    OriginRestriction onlyTmpOriginRestriction = OriginRestriction.denyByDefault().allowingDirectory(tempDir, true);

    // This should fail
    try {
      LoaderBuilder
          .anIsolatingLoader()
          .withOriginRestriction(onlyTmpOriginRestriction)
          .withClasspath(Collections.singletonList(getTestJarUri("cytodynamics-test-a")))
          .withParentRelationship(DelegateRelationshipBuilder.builder()
              .withIsolationLevel(IsolationLevel.FULL)
              .addWhitelistedClassPredicate(new GlobMatcher("java.*"))
              .build())
          .build();
      fail("Should have thrown an exception due to the origin restriction");
    } catch (OriginValidationException e) {
      // Expected
    }

    // This should succeed
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(onlyTmpOriginRestriction)
        .withClasspath(Collections.singletonList(destinationFile.toURI()))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPredicate(new GlobMatcher("java.*"))
            .addWhitelistedClassPredicate(new GlobMatcher("com.intellij.*"))
            .build())
        .build();

    TestInterface implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertEquals(implementation.getValue(), "A");
  }

  private static Class<?> findTestInterface(Class<?> implClass) {
    List<Class<?>> foundInterfaces = Stream.of(implClass.getInterfaces())
        .filter(clazz -> clazz.getName().equals(TestInterface.class.getName()))
        .collect(Collectors.toList());
    assertEquals(foundInterfaces.size(), 1);
    return foundInterfaces.iterator().next();
  }

  private static String readLine(InputStream inputStream) throws IOException {
    try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.defaultCharset());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
      return bufferedReader.readLine();
    }
  }



  /**
   * Validates resource loading.
   *
   * @param loader {@link ClassLoader} under test to load resources from
   * @param resourceContent expected content in the resources to be returned, in expected order
   */
  private static void assertResourcesFound(ClassLoader loader, String resourceName, List<String> resourceContent)
      throws IOException {
    URL dataUrl = loader.getResource(resourceName);
    assertEquals(readLine(dataUrl.openStream()), resourceContent.get(0));
    assertEquals(readLine(loader.getResourceAsStream(resourceName)), resourceContent.get(0));
    Enumeration<URL> dataUrls = loader.getResources(resourceName);
    for (String expectedContent : resourceContent) {
      assertTrue(
          String.format("Expected to find resource with content %s, but no more resources found", expectedContent),
          dataUrls.hasMoreElements());
      assertEquals(readLine(dataUrls.nextElement().openStream()), expectedContent);
    }
    assertFalse(dataUrls.hasMoreElements());
  }
}

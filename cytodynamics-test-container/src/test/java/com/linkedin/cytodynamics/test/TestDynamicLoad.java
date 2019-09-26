/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.test;

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
import org.testng.annotations.Test;

import static com.linkedin.cytodynamics.util.JarUtil.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;


/**
 * FIXME Document me!
 */
public class TestDynamicLoad {
  private static final String DATA_TXT_RESOURCE_NAME = "data.txt";

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
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
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
  public void testLoadResources() throws IOException {
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .build())
        .build();

    URL dataUrl = loader.getResource(DATA_TXT_RESOURCE_NAME);
    assertEquals(readLine(dataUrl.openStream()), "A");

    assertEquals(readLine(loader.getResourceAsStream(DATA_TXT_RESOURCE_NAME)), "A");

    Enumeration<URL> dataUrls = loader.getResources(DATA_TXT_RESOURCE_NAME);
    // should only have a single resource
    assertEquals(readLine(dataUrls.nextElement().openStream()), "A");
    assertFalse(dataUrls.hasMoreElements());
  }

  /**
   * This tests that resources will only be loaded from the direct classpath and not the parent nor the fallback.
   */
  @Test
  public void testLoadResourcesIsolation() throws IOException {
    // parent and fallback can have the same classpath, since we are just testing that they aren't used
    ClassLoader parentClassLoaderA = new URLClassLoader(new URL[]{getJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader fallbackClassLoaderA = new URLClassLoader(new URL[]{getJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-b")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(parentClassLoaderA)
            .withIsolationLevel(IsolationLevel.NONE)
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderA)
            .withIsolationLevel(IsolationLevel.NONE)
            .build())
        .build();

    URL dataUrl = loader.getResource(DATA_TXT_RESOURCE_NAME);
    assertEquals(readLine(dataUrl.openStream()), "B");

    assertEquals(readLine(loader.getResourceAsStream(DATA_TXT_RESOURCE_NAME)), "B");

    Enumeration<URL> dataUrls = loader.getResources(DATA_TXT_RESOURCE_NAME);
    // should only have a single resource
    assertEquals(readLine(dataUrls.nextElement().openStream()), "B");
    assertFalse(dataUrls.hasMoreElements());
  }

  @Test
  public void testLoadB() throws Exception {
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-b")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    TestInterface implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertEquals(implementation.getValue(), "B");
  }

  @Test(description = "Given that the loader is a parent of another classloader, then it should still properly do "
      + "classloading")
  public void testLoadAsParent() throws Exception {
    URI apiJarUri = getJarUri("cytodynamics-test-api");
    // need cytodynamics-nucleus for Api annotation in parent
    URL cytodynamics = getJarUri("cytodynamics-nucleus").toURL();
    ClassLoader apiClassLoader = new URLClassLoader(new URL[]{cytodynamics, apiJarUri.toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Arrays.asList(apiJarUri, getJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(apiClassLoader)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .build();

    // build a URLClassLoader with no classpath so only the parent would be used
    ClassLoader mainClassLoader = new URLClassLoader(new URL[]{}, loader);
    Class<?> testInterfaceClass = mainClassLoader.loadClass(TestInterface.class.getName());
    assertEquals(testInterfaceClass.getClassLoader(), apiClassLoader,
        "Should delegate up to the API classloader for the API class");
    Class<?> nonApiTestInterface = mainClassLoader.loadClass(NonApiTestInterface.class.getName());
    assertEquals(nonApiTestInterface.getClassLoader(), loader,
        "If there is a non-API class which is also in API classloader, shouldn't load from API classloader");
    Class<?> testInterfaceImplClass = mainClassLoader.loadClass(TestInterfaceImpl.class.getName());
    assertEquals(testInterfaceImplClass.getClassLoader(), loader,
        "Should delegate up to the isolating loader for the concrete (non-API) class");
  }

  @Test
  public void testIsolation() throws Exception {
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
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
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.lang.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    TestInterface implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-a")))
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
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-a")))
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
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addBlacklistedClassPattern("java.util.Set")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    TestInterface implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.TRANSITIONAL)
            .addBlacklistedClassPattern("java.util.*")
            .build())
        .build();

    implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.NONE)
            .addBlacklistedClassPattern("java.util.Set")
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
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    ClassLoader loaderB = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-b")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    TestInterface implementationA = (TestInterface) loaderA.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    TestInterface implementationB = (TestInterface) loaderB.loadClass(TestInterfaceImpl.class.getName()).newInstance();

    assertEquals(implementationA.getValue(), "A");
    assertEquals(implementationB.getValue(), "B");
  }

  @Test(description = "Given that there is a parent and a fallback delegate, and all can load a class, the class "
      + "should be loaded from the parent")
  public void testLoadFromParentNotFallback() throws Exception {
    URL testApiJarURL = getJarUri("cytodynamics-test-api").toURL();
    ClassLoader parentClassLoaderA =
        new URLClassLoader(new URL[]{testApiJarURL, getJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader fallbackClassLoaderB =
        new URLClassLoader(new URL[]{testApiJarURL, getJarUri("cytodynamics-test-b").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        // will load class from parent, so don't need any classpath
        .withClasspath(Collections.emptyList())
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(parentClassLoaderA)
            // using NONE so that we can load the class from the parent
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderB)
            // NONE so that the class could potentially be loaded from here (shouldn't actually be loaded from here)
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .build();
    Class<?> clazz = loader.loadClass(TestInterfaceImpl.class.getName());
    assertEquals(clazz.getClassLoader(), parentClassLoaderA);
  }

  @Test(description = "Given that there is a parent and a fallback delegate, but only the fallback parent has a "
      + "class, the class should be loaded by the fallback")
  public void testLoadFromFallback() throws Exception {
    URL testApiJarURL = getJarUri("cytodynamics-test-api").toURL();
    ClassLoader parentClassLoaderA =
        new URLClassLoader(new URL[]{testApiJarURL, getJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader fallbackClassLoaderA =
        new URLClassLoader(new URL[]{testApiJarURL, getJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader fallbackClassLoaderB =
        new URLClassLoader(new URL[]{testApiJarURL, getJarUri("cytodynamics-test-b").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        // will load class from parent, so don't need any classpath
        .withClasspath(Collections.emptyList())
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(parentClassLoaderA)
            // using FULL so that the implementation class is not loaded from the parent
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderB)
            // using NONE so that the class is loaded from here
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderA)
            // NONE so that the class could potentially be loaded from here (shouldn't actually be loaded from here)
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .build();
    Class<?> clazz = loader.loadClass(TestInterfaceImpl.class.getName());
    assertEquals(clazz.getClassLoader(), fallbackClassLoaderB);
  }

  @Test(description = "Given that there is a parent and a fallback delegate, but only the final fallback has a class, "
      + "the class should be loaded by that final fallback")
  public void testLoadFromFinalFallback() throws Exception {
    URL testApiJarURL = getJarUri("cytodynamics-test-api").toURL();
    ClassLoader parentClassLoaderB =
        new URLClassLoader(new URL[]{testApiJarURL, getJarUri("cytodynamics-test-b").toURL()}, null);
    ClassLoader fallbackClassLoaderB =
        new URLClassLoader(new URL[]{testApiJarURL, getJarUri("cytodynamics-test-b").toURL()}, null);
    ClassLoader fallbackClassLoaderA =
        new URLClassLoader(new URL[]{testApiJarURL, getJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        // will load class from a delegate, so don't need any classpath
        .withClasspath(Collections.emptyList())
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(parentClassLoaderB)
            // using FULL so that the implementation class is not loaded from the parent
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderB)
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderA)
            // NONE so class can be loaded form here
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .build();
    Class<?> clazz = loader.loadClass(TestInterfaceAOnlyImpl.class.getName());
    assertEquals(clazz.getClassLoader(), fallbackClassLoaderA);
  }

  /**
   * This also tests that a {@link ClassNotFoundException} is thrown when a class can't be found.
   */
  @Test(description = "Given FULL isolation level set for the parent and fallback, and a class is not an API, an "
      + "exception should be thrown",
      expectedExceptions = ClassNotFoundException.class)
  public void testIsolationForFallback() throws Exception {
    URL testApiJarURL = getJarUri("cytodynamics-test-api").toURL();
    ClassLoader parentClassLoaderA =
        new URLClassLoader(new URL[]{testApiJarURL, getJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader fallbackClassLoaderA =
        new URLClassLoader(new URL[]{testApiJarURL, getJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.emptyList())
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(parentClassLoaderA)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderA)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .build();
    loader.loadClass(TestInterfaceImpl.class.getName());
  }

  /**
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
  @Test(description = "Given a structure in which the classloader relationships form a graph, and two of the loaders "
      + "have a common parent, then delegation should properly load from the correct loaders")
  public void testGraphRelationshipWithCommonParent() throws Exception {
    URL apiJarUrl = getJarUri("cytodynamics-test-api").toURL();
    // need cytodynamics-nucleus for Api annotation in parent
    URL cytodynamics = getJarUri("cytodynamics-nucleus").toURL();
    ClassLoader commonParent = new URLClassLoader(new URL[]{cytodynamics, apiJarUrl}, null);
    ClassLoader partialDelegation =
        new URLClassLoader(new URL[]{getJarUri("cytodynamics-test-a").toURL()}, commonParent);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-b")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(commonParent)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(partialDelegation)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPattern("java.*")
            // only load concrete classes from fallback; don't load API classes from fallback
            .addDelegatePreferredClassPattern(TestInterfaceImpl.class.getName())
            .addDelegatePreferredClassPattern(TestInterfaceAOnlyImpl.class.getName())
            .build())
        .build();

    Object testInterfaceImpl = loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertEquals(testInterfaceImpl.getClass().getClassLoader(), loader,
        "TestInterfaceImpl needs to come from the main loader");
    Class<?> testInterfaceImplInterface = findTestInterface(testInterfaceImpl.getClass());
    assertEquals(testInterfaceImplInterface.getClassLoader(), commonParent,
        "TestInterface which is implemented by TestInterfaceImpl needs to come from the common parent");
    Object testInterfaceAOnlyImpl = loader.loadClass(TestInterfaceAOnlyImpl.class.getName()).newInstance();
    assertEquals(testInterfaceAOnlyImpl.getClass().getClassLoader(), partialDelegation,
        "TestInterfaceAOnlyImpl needs to come from the partial delegation classpath");
    Class<?> testInterfaceAOnlyImplInterface = findTestInterface(testInterfaceAOnlyImpl.getClass());
    assertEquals(testInterfaceAOnlyImplInterface.getClassLoader(), commonParent,
        "TestInterfaceAOnlyImpl which is implemented by TestInterfaceImpl needs to come from the common parent");
    // since these are both loaded from the common parent, then this should always be true, but double checking anyways
    assertEquals(testInterfaceImplInterface, testInterfaceAOnlyImplInterface,
        "TestInterface Class should be the same for TestInterfaceImpl and TestInterfaceAOnlyImpl");
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
        .withClasspath(Collections.singletonList(getJarUri(jarToUse)))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .addDelegatePreferredClassPattern("com.linkedin.cytodynamics.*")
            .build())
        .build();

    TestInterface childImplementation =
        (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertEquals(childImplementation.getValue(), implementation.getValue());
    assertEquals(childImplementation.getClass(), implementation.getClass());
  }

  @Test(enabled = false)
  public void testRepeatedLoad() throws Exception {
    System.out.println("Waiting to start");
    Thread.sleep(10000);

    for (int i = 0; i < 1000000; i++) {
      ClassLoader loader = LoaderBuilder
          .anIsolatingLoader()
          .withOriginRestriction(OriginRestriction.allowByDefault())
          .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-a")))
          .withParentRelationship(DelegateRelationshipBuilder.builder()
              .withIsolationLevel(IsolationLevel.FULL)
              .addWhitelistedClassPattern("java.*")
              .build())
          .build();

      TestInterface implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
      assertTrue(implementation.classExists("java.lang.String"));
      assertFalse(implementation.classExists(this.getClass().getName()));
    }

    System.gc();

    System.out.println("Waiting");
    Thread.sleep(Long.MAX_VALUE);
  }

  @Test
  public void testSecurity() throws Exception {
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    Path sourcePath = new File(getJarUri("cytodynamics-test-a")).toPath();
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
          .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-a")))
          .withParentRelationship(DelegateRelationshipBuilder.builder()
              .withIsolationLevel(IsolationLevel.FULL)
              .addWhitelistedClassPattern("java.*")
              .build())
          .build();
      fail("Should have thrown a security exception");
    } catch (SecurityException e) {
      // Expected
    }

    // This should succeed
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(onlyTmpOriginRestriction)
        .withClasspath(Collections.singletonList(destinationFile.toURI()))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
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
}

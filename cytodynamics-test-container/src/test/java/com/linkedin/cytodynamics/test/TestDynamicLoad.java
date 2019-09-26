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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.linkedin.cytodynamics.nucleus.DelegateRelationshipBuilder;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;


/**
 * FIXME Document me!
 */
public class TestDynamicLoad {
  private static File findBaseDirectory() {
    File currentDirectory;
    try {
      currentDirectory = new File(".").getCanonicalFile();
    } catch (IOException e) {
      fail("Failed to find the base directory", e);
      return null;
    }

    File testADirectory = new File(currentDirectory, "cytodynamics-test-a");
    if (testADirectory.exists() && testADirectory.isDirectory()) {
      return currentDirectory;
    }

    while (currentDirectory.getParentFile() != null) {
      currentDirectory = currentDirectory.getParentFile();

      testADirectory = new File(currentDirectory, "cytodynamics-test-a");
      if (testADirectory.exists() && testADirectory.isDirectory()) {
        return currentDirectory;
      }
    }

    fail("Failed to find the base directory");
    return null;
  }

  static URI getTestJarUri(String whichOne) {
    File targetDir = new File(
        new File(findBaseDirectory(), "cytodynamics-test-" + whichOne),
        "target");

    if (!targetDir.exists()) {
      fail("No target directory exists");
    }

    //
    File[] filesInTargetDir = targetDir.listFiles();
    for (File fileInTargetDir : filesInTargetDir) {
      if (fileInTargetDir.getName().startsWith("cytodynamics-test") &&
          fileInTargetDir.getName().endsWith(".jar") &&
          !fileInTargetDir.getName().contains("sources") &&
          !fileInTargetDir.getName().contains("javadoc")) {
        return fileInTargetDir.toURI();
      }
    }

    fail("Failed to find the test jar in the target directory");
    return null;
  }

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
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
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
  public void testLoadB() throws Exception {
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("b")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    TestInterface implementation = (TestInterface) loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertEquals(implementation.getValue(), "B");
  }

  @Test
  public void testIsolation() throws Exception {
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
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
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
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
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
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
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
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
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
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
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
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
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
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
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    ClassLoader loaderB = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("b")))
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
    URL testApiJarURL = getTestJarUri("api").toURL();
    ClassLoader parentClassLoaderA = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("a").toURL()}, null);
    ClassLoader fallbackClassLoaderB = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("b").toURL()}, null);
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
            // using NONE so that the class could potentially be loaded from here
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
    URL testApiJarURL = getTestJarUri("api").toURL();
    ClassLoader parentClassLoaderA = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("a").toURL()}, null);
    ClassLoader fallbackClassLoaderB = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("b").toURL()}, null);
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
            // using NONE so that the class could potentially be loaded from here
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
    URL testApiJarURL = getTestJarUri("api").toURL();
    ClassLoader parentClassLoaderB = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("b").toURL()}, null);
    ClassLoader fallbackClassLoaderB = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("b").toURL()}, null);
    ClassLoader fallbackClassLoaderA = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("a").toURL()}, null);
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
            // using NONE so that the class could potentially be loaded from here
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallbackClassLoaderA)
            // using NONE so that the class could potentially be loaded from here
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
    URL testApiJarURL = getTestJarUri("api").toURL();
    ClassLoader parentClassLoaderA = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("a").toURL()}, null);
    ClassLoader fallbackClassLoaderA = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("a").toURL()}, null);
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
    URL commonParentJarUrl = getTestJarUri("api").toURL();
    ClassLoader commonParent = new URLClassLoader(new URL[]{commonParentJarUrl}, null);
    ClassLoader partialDelegation = new URLClassLoader(new URL[]{getTestJarUri("a").toURL()}, commonParent);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("b")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(commonParent)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPattern("java.*")
            // TODO fix: when loader's classloader doesn't match the parent classloader, Api annotation doesn't apply
            .addDelegatePreferredClassPattern(TestInterface.class.getName())
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(partialDelegation)
            // using NONE so that the class could potentially be loaded from here
            .withIsolationLevel(IsolationLevel.NONE)
            .addDelegatePreferredClassPattern("java.*")
            .build())
        .build();

    /*
     * If we tried to assign to a TestInterface variable directly, it would not work, because the TestInterface for the
     * actual objects are loaded from the commonParent and not the classloader which is associated with the execution of
     * this test.
     * This means we need to use reflection to call methods on the objects.
     */
    Object testInterfaceImpl = loader.loadClass(TestInterfaceImpl.class.getName()).newInstance();
    assertEquals(testInterfaceImpl.getClass().getMethod("getValue").invoke(testInterfaceImpl), "B",
        "TestInterfaceImpl needs to come from the main loader classpath");
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
      jarToUse = "b";
    } else {
      jarToUse = "a";
    }

    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri(jarToUse)))
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
          .withClasspath(Collections.singletonList(getTestJarUri("a")))
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
    Path sourcePath = new File(getTestJarUri("a")).toPath();
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
          .withClasspath(Collections.singletonList(getTestJarUri("a")))
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
}

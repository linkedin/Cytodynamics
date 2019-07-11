/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.test;

import com.linkedin.cytodynamics.nucleus.IsolationLevel;
import com.linkedin.cytodynamics.nucleus.Loader;
import com.linkedin.cytodynamics.nucleus.LoaderBuilder;
import com.linkedin.cytodynamics.nucleus.OriginRestriction;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
import com.linkedin.cytodynamics.nucleus.ParentRelationshipBuilder;
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
  public void testLoadA() {
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertEquals(implementation.getValue(), "A");
  }

  @Test
  public void testLoadB() {
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("b")))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertEquals(implementation.getValue(), "B");
  }

  @Test
  public void testIsolation() {
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists(this.getClass().getName()));
  }

  @Test
  public void testIsolationLevels() {
    // Full isolation
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.lang.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.TRANSITIONAL)
            .build())
        .build();

    implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertTrue(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.NONE)
            .build())
        .build();

    implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertTrue(implementation.classExists("java.util.Set"));
  }

  @Test
  public void testBlacklist() {
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addBlacklistedClassPattern("java.util.Set")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.TRANSITIONAL)
            .addBlacklistedClassPattern("java.util.*")
            .build())
        .build();

    implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.NONE)
            .addBlacklistedClassPattern("java.util.Set")
            .build())
        .build();

    implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));
  }

  @Test
  public void testLoadAandB() {
    Loader loaderA = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    Loader loaderB = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("b")))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    TestInterface implementationA = loaderA.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    TestInterface implementationB = loaderB.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());

    assertEquals(implementationA.getValue(), "A");
    assertEquals(implementationB.getValue(), "B");
  }

  @Test(description = "Given that there are multiple parent relationships, and all can load a class, the class should "
      + "be loaded from the first parent relationship")
  public void testMultipleParentsLoadFromFirstParent() throws MalformedURLException {
    URL testApiJarURL = getTestJarUri("api").toURL();
    ClassLoader parentClassLoaderA = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("a").toURL()}, null);
    ClassLoader parentClassLoaderB = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("b").toURL()}, null);
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        // will load class from parent, so don't need any classpath
        .withClasspath(Collections.emptyList())
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withParentClassLoader(parentClassLoaderA)
            // using NONE so that we can load the class from the parent
            .withIsolationLevel(IsolationLevel.NONE)
            .addParentPreferredClassPattern("java.*")
            .build())
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withParentClassLoader(parentClassLoaderB)
            .withIsolationLevel(IsolationLevel.NONE)
            .build())
        .build();
    Class<?> clazz = loader.loadClass(Object.class, TestInterfaceImpl.class.getName());
    assertEquals(clazz.getClassLoader(), parentClassLoaderA);
  }

  @Test(description = "Given that there are multiple parent relationships, but only the last parent has a class, the "
      + "class should be loaded by the last parent")
  public void testMultipleParentsLoadFromLastParent() throws MalformedURLException {
    URL testApiJarURL = getTestJarUri("api").toURL();
    ClassLoader parentClassLoaderA = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("a").toURL()}, null);
    ClassLoader parentClassLoaderB = new URLClassLoader(new URL[]{testApiJarURL, getTestJarUri("b").toURL()}, null);
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        // will load class from parent, so don't need any classpath
        .withClasspath(Collections.emptyList())
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withParentClassLoader(parentClassLoaderA)
            // using FULL so that the implementation class is not loaded from this parent
            .withIsolationLevel(IsolationLevel.FULL)
            .addParentPreferredClassPattern("java.*")
            .build())
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withParentClassLoader(parentClassLoaderB)
            // using NONE so that we can load the class from the parent
            .withIsolationLevel(IsolationLevel.NONE)
            .build())
        .build();
    Class<?> clazz = loader.loadClass(Object.class, TestInterfaceImpl.class.getName());
    assertEquals(clazz.getClassLoader(), parentClassLoaderB);
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
   * A more concrete use case for this is that the common parent would contain some shared API interfaces that need to
   * be used by both the partial delegation classloader and the child classloader. The partial delegation classloader
   * and the child classloader could be managed by different owners, so they would need to be separate and isolated.
   *
   * It should not be possible to build a cyclic graph with the cytodynamics builder APIs, so we are just considering an
   * acyclic graph here.
   */
  @Test(description = "Given a structure in which the parent relationships form a graph, and two of the loaders have "
      + "a common parent, then delegation should properly load from the correct loaders")
  public void testGraphRelationshipWithCommonParent() throws Exception {
    URL commonParentJarUrl = getTestJarUri("api").toURL();
    ClassLoader commonParent = new URLClassLoader(new URL[]{commonParentJarUrl}, null);
    ClassLoader partialDelegation = new URLClassLoader(new URL[]{getTestJarUri("a").toURL()}, commonParent);
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri("b")))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withParentClassLoader(commonParent)
            .withIsolationLevel(IsolationLevel.FULL)
            .addParentPreferredClassPattern("java.*")
            // TODO fix: when loader's classloader doesn't match the parent classloader, Api annotation doesn't apply
            .addParentPreferredClassPattern(TestInterface.class.getName())
            .build())
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withParentClassLoader(partialDelegation)
            .withIsolationLevel(IsolationLevel.NONE)
            .build())
        .build();

    /*
     * If we tried to assign to a TestInterface variable directly, it would not work, because the TestInterface for the
     * actual objects are loaded from the commonParent and not the classloader which is associated with the execution of
     * this test.
     * This means we need to use reflection to call methods on the objects.
     */
    Object testInterfaceImpl = loader.newInstanceOf(Object.class, TestInterfaceImpl.class.getName());
    // check that the TestInterfaceImpl comes from the loader classpath
    assertEquals(testInterfaceImpl.getClass().getMethod("getValue").invoke(testInterfaceImpl), "B");
    // check that the TestInterface interface implemented by TestInterfaceImpl is loaded by the common parent
    Class<?> testInterfaceImplInterface = findTestInterface(testInterfaceImpl.getClass());
    assertEquals(testInterfaceImplInterface.getClassLoader(), commonParent);
    Object testInterfaceAOnlyImpl = loader.newInstanceOf(Object.class, TestInterfaceAOnlyImpl.class.getName());
    // check that the TestInterfaceAOnlyImpl comes from the partial delegation classpath
    assertEquals(testInterfaceAOnlyImpl.getClass().getClassLoader(), partialDelegation);
    // check that the TestInterface interface implemented by TestInterfaceAOnlyImpl is loaded by the common parent
    Class<?> testInterfaceAOnlyImplInterface = findTestInterface(testInterfaceAOnlyImpl.getClass());
    assertEquals(testInterfaceAOnlyImplInterface.getClassLoader(), commonParent);
    // since these are both loaded from the common parent, then this should always be true, but double checking anyways
    assertEquals(testInterfaceImplInterface, testInterfaceAOnlyImplInterface);
  }

  @Test
  public void testParentPreferred() {
    TestInterface implementation = new TestInterfaceImpl();
    String parentValue = implementation.getValue();


    String jarToUse;
    if (parentValue.equals("A")) {
      jarToUse = "b";
    } else {
      jarToUse = "a";
    }

    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getTestJarUri(jarToUse)))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .addParentPreferredClassPattern("com.linkedin.cytodynamics.*")
            .build())
        .build();

    TestInterface childImplementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertEquals(childImplementation.getValue(), implementation.getValue());
    assertEquals(childImplementation.getClass(), implementation.getClass());
  }

  @Test(enabled = false)
  public void testRepeatedLoad() throws Exception {
    System.out.println("Waiting to start");
    Thread.sleep(10000);

    for (int i = 0; i < 1000000; i++) {
      Loader loader = LoaderBuilder
          .anIsolatingLoader()
          .withOriginRestriction(OriginRestriction.allowByDefault())
          .withClasspath(Collections.singletonList(getTestJarUri("a")))
          .addParentRelationship(ParentRelationshipBuilder.builder()
              .withIsolationLevel(IsolationLevel.FULL)
              .addWhitelistedClassPattern("java.*")
              .build())
          .build();

      TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
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
      Loader loader = LoaderBuilder
          .anIsolatingLoader()
          .withOriginRestriction(onlyTmpOriginRestriction)
          .withClasspath(Collections.singletonList(getTestJarUri("a")))
          .addParentRelationship(ParentRelationshipBuilder.builder()
              .withIsolationLevel(IsolationLevel.FULL)
              .addWhitelistedClassPattern("java.*")
              .build())
          .build();

      TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
      fail("Should have thrown a security exception");
    } catch (SecurityException e) {
      // Expected
    }

    // This should succeed
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(onlyTmpOriginRestriction)
        .withClasspath(Collections.singletonList(destinationFile.toURI()))
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .addWhitelistedClassPattern("com.intellij.*")
            .build())
        .build();

    TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
  }

  private static Class<?> findTestInterface(Class<?> implClass) {
    List<Class<?>> foundInterfaces = Stream.of(implClass.getInterfaces())
        .filter(clazz -> clazz.getName().equals(TestInterface.class.getName()))
        .collect(Collectors.toList());
    assertEquals(foundInterfaces.size(), 1);
    return foundInterfaces.iterator().next();
  }
}

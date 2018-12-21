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
package com.linkedin.cytodynamics.test;

import com.linkedin.cytodynamics.nucleus.IsolationLevel;
import com.linkedin.cytodynamics.nucleus.Loader;
import com.linkedin.cytodynamics.nucleus.LoaderBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
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
      if (fileInTargetDir.getName().startsWith("cytodynamics-test") && fileInTargetDir.getName().endsWith(".jar")) {
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
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .withIsolationLevel(IsolationLevel.FULL)
        .addWhitelistedClassPattern("java.*")
        .addWhitelistedClassPattern("com.intellij.*")
        .build();

    TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertEquals(implementation.getValue(), "A");
  }

  @Test
  public void testLoadB() {
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(Collections.singletonList(getTestJarUri("b")))
        .withIsolationLevel(IsolationLevel.FULL)
        .addWhitelistedClassPattern("java.*")
        .addWhitelistedClassPattern("com.intellij.*")
        .build();

    TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertEquals(implementation.getValue(), "B");
  }

  @Test
  public void testIsolation() {
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .withIsolationLevel(IsolationLevel.FULL)
        .addWhitelistedClassPattern("java.*")
        .addWhitelistedClassPattern("com.intellij.*")
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
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .withIsolationLevel(IsolationLevel.FULL)
        .addWhitelistedClassPattern("java.lang.*")
        .addWhitelistedClassPattern("com.intellij.*")
        .build();

    TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .withIsolationLevel(IsolationLevel.TRANSITIONAL)
        .build();

    implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertTrue(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .withIsolationLevel(IsolationLevel.NONE)
        .build();

    implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertTrue(implementation.classExists("java.util.Set"));
  }

  @Test
  public void testBlacklist() {
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .withIsolationLevel(IsolationLevel.FULL)
        .addWhitelistedClassPattern("java.*")
        .addBlacklistedClassPattern("java.util.Set")
        .addWhitelistedClassPattern("com.intellij.*")
        .build();

    TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .withIsolationLevel(IsolationLevel.TRANSITIONAL)
        .addBlacklistedClassPattern("java.util.*")
        .build();

    implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));

    loader = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .withIsolationLevel(IsolationLevel.NONE)
        .addBlacklistedClassPattern("java.util.Set")
        .build();

    implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    assertTrue(implementation.classExists("java.lang.String"));
    assertFalse(implementation.classExists("java.util.Set"));
  }

  @Test
  public void testLoadAandB() {
    Loader loaderA = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(Collections.singletonList(getTestJarUri("a")))
        .withIsolationLevel(IsolationLevel.FULL)
        .addWhitelistedClassPattern("java.*")
        .addWhitelistedClassPattern("com.intellij.*")
        .build();

    Loader loaderB = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(Collections.singletonList(getTestJarUri("b")))
        .withIsolationLevel(IsolationLevel.FULL)
        .addWhitelistedClassPattern("java.*")
        .addWhitelistedClassPattern("com.intellij.*")
        .build();

    TestInterface implementationA = loaderA.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
    TestInterface implementationB = loaderB.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());

    assertEquals(implementationA.getValue(), "A");
    assertEquals(implementationB.getValue(), "B");
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
        .withClasspath(Collections.singletonList(getTestJarUri(jarToUse)))
        .withIsolationLevel(IsolationLevel.FULL)
        .addWhitelistedClassPattern("java.*")
        .addWhitelistedClassPattern("com.intellij.*")
        .addParentPreferredClassPattern("com.linkedin.cytodynamics.*")
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
          .withClasspath(Collections.singletonList(getTestJarUri("a")))
          .withIsolationLevel(IsolationLevel.FULL)
          .build();

      TestInterface implementation = loader.newInstanceOf(TestInterface.class, TestInterfaceImpl.class.getName());
      assertTrue(implementation.classExists("java.lang.String"));
      assertFalse(implementation.classExists(this.getClass().getName()));
    }

    System.gc();

    System.out.println("Waiting");
    Thread.sleep(Long.MAX_VALUE);
  }
}

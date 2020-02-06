/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import com.linkedin.cytodynamics.matcher.GlobMatcher;
import com.linkedin.cytodynamics.test.TestInterface;
import com.linkedin.cytodynamics.test.TestInterfaceAOnlyImpl;
import com.linkedin.cytodynamics.test.TestInterfaceImpl;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.linkedin.cytodynamics.util.JarUtil.*;
import static org.mockito.Mockito.*;


/**
 * This class is in the com.linkedin.cytodynamics.nucleus so that it can have access to
 * {@link IsolatingClassLoader#loadClass(String, boolean)} for testing.
 * Even though it is protected, this method can be considered part of the API of this class, since it can be called from
 * {@link ClassLoader}.
 */
public class TestLoadClassResolve {
  private IsolatingClassLoader isolatingClassLoader;

  @BeforeMethod
  public void setup() throws Exception {
    URL cytodynamics = getJarUri("cytodynamics-nucleus").toURL();
    URL testApiJarURL = getJarUri("cytodynamics-test-api").toURL();
    ClassLoader parent = new URLClassLoader(new URL[]{cytodynamics, testApiJarURL}, null);
    ClassLoader fallback =
        new URLClassLoader(new URL[]{cytodynamics, testApiJarURL, getJarUri("cytodynamics-test-a").toURL()}, null);
    ClassLoader loader = LoaderBuilder
        .anIsolatingLoader()
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .withClasspath(Collections.singletonList(getJarUri("cytodynamics-test-b")))
        .withParentRelationship(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(parent)
            .withIsolationLevel(IsolationLevel.FULL)
            .addDelegatePreferredClassPredicate(new GlobMatcher("java.*"))
            .build())
        .addFallbackDelegate(DelegateRelationshipBuilder.builder()
            .withDelegateClassLoader(fallback)
            .withIsolationLevel(IsolationLevel.FULL)
            // only load concrete classes from fallback; don't load API classes from fallback
            .addDelegatePreferredClassPredicate(new GlobMatcher(TestInterfaceAOnlyImpl.class.getName()))
            .addBlacklistedClassPredicate(new GlobMatcher(TestInterface.class.getName()))
            .build())
        .build();
    /*
     * Cast to IsolatingClassLoader so we can call the protected loadClass directly.
     * Using a spy to verify method calls to resolveClass. It's unnecessary to actually mock resolveClass.
     */
    this.isolatingClassLoader = spy((IsolatingClassLoader) loader);
  }

  @Test(description = "Given that loadClass is called with the resolve argument as false, loadClass should properly "
      + "skip the resolve step")
  public void testLoadClassWithResolveFalse() throws Exception {
    // from parent classloader
    this.isolatingClassLoader.loadClass(TestInterface.class.getName(), false);
    // directly from isolating loader
    this.isolatingClassLoader.loadClass(TestInterfaceImpl.class.getName(), false);
    // from fallback classloader
    this.isolatingClassLoader.loadClass(TestInterfaceAOnlyImpl.class.getName(), false);
    verify(this.isolatingClassLoader, never()).doResolveClass(any());
  }

  @Test(description = "Given a class to be loaded from the parent classloader and resolve as true, loadClass should "
      + "execute the resolve step")
  public void testLoadClassWithResolveTrueFromParent() throws Exception {
    Class<?> testInterfaceClass = this.isolatingClassLoader.loadClass(TestInterface.class.getName(), true);
    verify(isolatingClassLoader).doResolveClass(testInterfaceClass);
  }

  @Test(description = "Given a class to be loaded from the classpath and resolve as true, loadClass should execute the "
      + "resolve step")
  public void testLoadClassWithResolveTrueFromClasspath() throws Exception {
    Class<?> testInterfaceImplClass = isolatingClassLoader.loadClass(TestInterfaceImpl.class.getName(), true);
    verify(isolatingClassLoader).doResolveClass(testInterfaceImplClass);
  }

  @Test(description = "Given a class to be loaded from the fallback classloader and resolve as true, loadClass should "
      + "execute the resolve step")
  public void testLoadClassWithResolveTrueFromFallback() throws Exception {
    Class<?> testInterfaceAOnlyImplClass = isolatingClassLoader.loadClass(TestInterfaceAOnlyImpl.class.getName(), true);
    verify(isolatingClassLoader).doResolveClass(testInterfaceAOnlyImplClass);
  }
}
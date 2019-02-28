/**
 * Copyright 2018 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class TestGlobMatcher {
  @Test
  public void testQuestionMark() {
    GlobMatcher matcher = new GlobMatcher("?oo");
    assertTrue(matcher.matches("foo"));
    assertTrue(matcher.matches("boo"));
    assertFalse(matcher.matches("waterloo"));
  }

  @Test
  public void testStar() {
    GlobMatcher matcher = new GlobMatcher("*ue");
    assertTrue(matcher.matches("rue"));
    assertTrue(matcher.matches("blue"));
    assertFalse(matcher.matches("bluer"));
  }

  @Test
  public void testMixed() {
    GlobMatcher matcher = new GlobMatcher("java.?ang.*");
    assertTrue(matcher.matches("java.lang.String"));
    assertTrue(matcher.matches("java.lang.Integer"));
    assertTrue(matcher.matches("java.lang.reflect.Method"));
    assertFalse(matcher.matches("java.util.List"));
  }

  @Test
  public void testEscaping() {
    GlobMatcher dotMatcher = new GlobMatcher(".");
    assertTrue(dotMatcher.matches("."));
    assertFalse(dotMatcher.matches("a"));

    GlobMatcher dotStarMatcher = new GlobMatcher(".*");
    assertTrue(dotStarMatcher.matches(".abcd"));
    assertFalse(dotStarMatcher.matches("abcd"));
  }
}

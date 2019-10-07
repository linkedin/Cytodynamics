/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.matcher;

import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class GlobMatcherTest {
  @Test
  public void testQuestionMark() {
    GlobMatcher matcher = new GlobMatcher("?oo");
    assertTrue(matcher.test("foo"));
    assertTrue(matcher.test("boo"));
    assertFalse(matcher.test("waterloo"));
  }

  @Test
  public void testStar() {
    GlobMatcher matcher = new GlobMatcher("*ue");
    assertTrue(matcher.test("rue"));
    assertTrue(matcher.test("blue"));
    assertFalse(matcher.test("bluer"));
  }

  @Test
  public void testMixed() {
    GlobMatcher matcher = new GlobMatcher("java.?ang.*");
    assertTrue(matcher.test("java.lang.String"));
    assertTrue(matcher.test("java.lang.Integer"));
    assertTrue(matcher.test("java.lang.reflect.Method"));
    assertFalse(matcher.test("java.util.List"));
  }

  @Test
  public void testEscaping() {
    GlobMatcher dotMatcher = new GlobMatcher(".");
    assertTrue(dotMatcher.test("."));
    assertFalse(dotMatcher.test("a"));

    GlobMatcher dotStarMatcher = new GlobMatcher(".*");
    assertTrue(dotStarMatcher.test(".abcd"));
    assertFalse(dotStarMatcher.test("abcd"));
  }
}

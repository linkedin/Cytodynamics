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
}

/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

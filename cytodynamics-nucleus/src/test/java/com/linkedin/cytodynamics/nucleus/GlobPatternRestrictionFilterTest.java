/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.net.URL;
import org.junit.Test;

import static org.junit.Assert.*;


public class GlobPatternRestrictionFilterTest {
  @Test
  public void testGlobPattern() throws Exception {
    GlobPatternRestrictionFilter allowExampleComHttpsFilter = new GlobPatternRestrictionFilter("https://example.com/*", OriginMatchResults.ALLOW);
    assertTrue(allowExampleComHttpsFilter.matches(new URL("https://example.com/")));
    assertTrue(allowExampleComHttpsFilter.matches(new URL("https://example.com/foo")));
    assertFalse(allowExampleComHttpsFilter.matches(new URL("http://example.com/")));
    assertFalse(allowExampleComHttpsFilter.matches(new URL("http://not.example.com/")));
  }
}

/**
 * Copyright 2018 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class ProtocolOriginRestrictionFilterTest {
  @Test
  public void testProtocolOriginRestrictionFilter() throws Exception {
    ProtocolOriginRestrictionFilter httpOnly = new
        ProtocolOriginRestrictionFilter(Collections.singleton("http"), OriginMatchResults.ALLOW);

    assertTrue(httpOnly.matches(new URL("http://www.example.com/")));
    assertEquals(httpOnly.isAllowed(new URL("http://www.example.com/")), OriginMatchResults.ALLOW);

    assertFalse(httpOnly.matches(new URL("https://www.example.com/")));
    assertEquals(httpOnly.isAllowed(new URL("https://www.example.com/")), OriginMatchResults.NO_MATCH);

    Set<String> protocolSet = new HashSet<>(Arrays.asList("http", "https"));
    ProtocolOriginRestrictionFilter httpAndHttpsDeny = new
        ProtocolOriginRestrictionFilter(protocolSet, OriginMatchResults.DENY);

    assertTrue(httpAndHttpsDeny.matches(new URL("http://www.example.com/")));
    assertEquals(httpAndHttpsDeny.isAllowed(new URL("http://www.example.com/")), OriginMatchResults.DENY);

    assertTrue(httpAndHttpsDeny.matches(new URL("https://www.example.com/")));
    assertEquals(httpAndHttpsDeny.isAllowed(new URL("https://www.example.com/")), OriginMatchResults.DENY);
  }
}

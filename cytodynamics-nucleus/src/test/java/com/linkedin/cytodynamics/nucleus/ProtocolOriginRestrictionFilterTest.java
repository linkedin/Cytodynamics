/**
 * Copyright 2018-2019 LinkedIn Corporation
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
import org.junit.Test;

import static org.junit.Assert.*;


public class ProtocolOriginRestrictionFilterTest {
  @Test
  public void testProtocolOriginRestrictionFilter() throws Exception {
    ProtocolOriginRestrictionFilter httpOnly = new
        ProtocolOriginRestrictionFilter(Collections.singleton("http"), OriginMatchResults.ALLOW);

    assertTrue(httpOnly.matches(new URL("http://www.example.com/")));
    assertEquals(OriginMatchResults.ALLOW, httpOnly.isAllowed(new URL("http://www.example.com/")));

    assertFalse(httpOnly.matches(new URL("https://www.example.com/")));
    assertEquals(OriginMatchResults.NO_MATCH, httpOnly.isAllowed(new URL("https://www.example.com/")));

    Set<String> protocolSet = new HashSet<>(Arrays.asList("http", "https"));
    ProtocolOriginRestrictionFilter httpAndHttpsDeny = new
        ProtocolOriginRestrictionFilter(protocolSet, OriginMatchResults.DENY);

    assertTrue(httpAndHttpsDeny.matches(new URL("http://www.example.com/")));
    assertEquals(OriginMatchResults.DENY, httpAndHttpsDeny.isAllowed(new URL("http://www.example.com/")));

    assertTrue(httpAndHttpsDeny.matches(new URL("https://www.example.com/")));
    assertEquals(OriginMatchResults.DENY, httpAndHttpsDeny.isAllowed(new URL("https://www.example.com/")));
  }
}

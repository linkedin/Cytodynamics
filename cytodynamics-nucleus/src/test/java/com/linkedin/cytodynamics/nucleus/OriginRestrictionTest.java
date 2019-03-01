/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.net.URL;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class OriginRestrictionTest {
  @Test
  public void testDefault() throws Exception {
    assertTrue(OriginRestriction.allowByDefault().isAllowed(new URL("http://foo/")));
    assertFalse(OriginRestriction.denyByDefault().isAllowed(new URL("http://foo/")));
  }

  @Test
  public void testProtocolAndFallback() throws Exception {
    OriginRestriction allowHttpsDenyDefault = OriginRestriction
        .denyByDefault()
        .allowingProtocols("https");

    assertFalse(allowHttpsDenyDefault.isAllowed(new URL("http://foo/")));
    assertTrue(allowHttpsDenyDefault.isAllowed(new URL("https://foo/")));

    OriginRestriction denyHttpAllowDefault = OriginRestriction
        .allowByDefault()
        .allowingProtocols("https")
        .denyingProtocols("http");

    assertFalse(denyHttpAllowDefault.isAllowed(new URL("http://foo/")));
    assertTrue(denyHttpAllowDefault.isAllowed(new URL("https://foo/")));
    assertTrue(denyHttpAllowDefault.isAllowed(new URL("file:///foo")));
  }
}

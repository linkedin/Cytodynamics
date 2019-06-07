/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import org.testng.annotations.Test;
import spark.Spark;

import static org.testng.Assert.*;


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

  @Test
  public void testFailRedirect() throws Exception {
    final String ALLOWED_JAR_LOCATION = "http://localhost:4567/test.jar";
    final String REDIRECTED_JAR_LOCATION = "http://central.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar";

    // Start HTTP server that redirects to another host
    Spark.get("/test.jar", (request, response) -> {
      response.redirect(REDIRECTED_JAR_LOCATION);
      return "";
    });

    OriginRestriction allowOnlyLocalhost = OriginRestriction
        .denyByDefault()
        .allowingGlobPattern(ALLOWED_JAR_LOCATION);

    // Shouldn't be able to load a class through a redirect
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(Collections.singletonList(new URI(ALLOWED_JAR_LOCATION)))
        .withOriginRestriction(allowOnlyLocalhost)
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .build())
        .build();

    Class clazz = loader.loadClass(Object.class, "org.apache.log4j.Logger");
    assertNull(clazz);

    // Should be able to load the class using the original URL
    loader = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(Collections.singletonList(new URI(REDIRECTED_JAR_LOCATION)))
        .withOriginRestriction(OriginRestriction.allowByDefault())
        .addParentRelationship(ParentRelationshipBuilder.builder()
            .withIsolationLevel(IsolationLevel.FULL)
            .addWhitelistedClassPattern("java.*")
            .build())
        .build();

    clazz = loader.loadClass(Object.class, "org.apache.log4j.Logger");
    assertNotNull(clazz);

    // Stop HTTP server
    Spark.stop();
  }
}

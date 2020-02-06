/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.matcher;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class BootstrapClassPredicateTest {
  private BootstrapClassPredicate bootstrapClassPredicate;

  @BeforeMethod
  public void setup() {
    this.bootstrapClassPredicate = new BootstrapClassPredicate();
  }

  @Test
  public void testBootstrapClass() {
    assertTrue(this.bootstrapClassPredicate.test("java.lang.String"));
    assertTrue(this.bootstrapClassPredicate.test("java.lang.Object"));
    assertTrue(this.bootstrapClassPredicate.test("java.lang.System"));
  }

  @Test
  public void testNotBootstrapClass() {
    assertFalse(this.bootstrapClassPredicate.test(this.getClass().getName()));
    assertFalse(this.bootstrapClassPredicate.test("com.linkedin.cytodynamics.nucleus.BootstrapClassPredicate"));
  }
}
/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.isolation;

import org.junit.Test;

import static org.junit.Assert.*;


public class TestNoneIsolationChooser {
  @Test
  public void testChoose() {
    String delegate = "delegateValue", child = "childValue";
    NoneIsolationChooser<String> chooser = new NoneIsolationChooser<>();
    assertEquals(child, chooser.choose(delegate, child));
    assertEquals(delegate, chooser.choose(delegate, null));
    assertEquals(child, chooser.choose(null, child));
    assertNull(chooser.choose(null, null));
  }
}
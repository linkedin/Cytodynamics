/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.isolation;

import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class TestFullIsolationChooser {
  @Test
  public void testChoose() {
    String delegate = "delegateValue", child = "childValue";
    FullIsolationChooser<String> chooser = new FullIsolationChooser<>();
    assertEquals(child, chooser.choose(delegate, child));
    assertNull(chooser.choose(delegate, null));
    assertEquals(child, chooser.choose(null, child));
    assertNull(chooser.choose(null, null));
  }
}
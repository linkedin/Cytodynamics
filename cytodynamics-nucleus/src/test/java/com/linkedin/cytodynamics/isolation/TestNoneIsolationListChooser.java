/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.isolation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.*;


public class TestNoneIsolationListChooser {
  @Test
  public void testChoose() {
    List<String> delegate = Collections.singletonList("delegateValue"), child =
        Arrays.asList("childValue0", "childValue1");
    NoneIsolationListChooser<String> chooser = new NoneIsolationListChooser<>();
    List<String> merged = new ArrayList<>(child);
    merged.addAll(delegate);
    assertEquals(merged, chooser.choose(delegate, child));
    assertEquals(delegate, chooser.choose(delegate, null));
    assertEquals(child, chooser.choose(null, child));
    assertNull(chooser.choose(null, null));
  }
}
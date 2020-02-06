/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.isolation;

import java.util.function.Consumer;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;


public class TestTransitionalIsolationChooser {
  @Test
  public void testChoose() {
    String delegate = "delegateValue", child = "childValue";
    TransitionalIsolationChooser<String> chooser = new TransitionalIsolationChooser<>(mock(Consumer.class));
    assertEquals(child, chooser.choose(delegate, child));
    assertEquals(delegate, chooser.choose(delegate, null));
    assertEquals(child, chooser.choose(null, child));
    assertNull(chooser.choose(null, null));
  }

  @Test
  public void testChooseReportDelegateUsageWarning() {
    String delegate = "delegateValue";
    Consumer<String> reportDelegateUsageWarning = mock(Consumer.class);
    TransitionalIsolationChooser<String> chooser = new TransitionalIsolationChooser<>(reportDelegateUsageWarning);
    assertEquals(delegate, chooser.choose(delegate, null));
    verify(reportDelegateUsageWarning).accept(delegate);
  }
}
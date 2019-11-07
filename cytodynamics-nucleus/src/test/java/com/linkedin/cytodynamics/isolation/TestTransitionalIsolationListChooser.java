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
import java.util.function.Consumer;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;


public class TestTransitionalIsolationListChooser {
  @Test
  public void testChoose() {
    List<String> delegate = Collections.singletonList("delegateValue"), child =
        Arrays.asList("childValue0", "childValue1");
    TransitionalIsolationListChooser<String> chooser = new TransitionalIsolationListChooser<>(mock(Consumer.class));
    List<String> merged = new ArrayList<>(child);
    merged.addAll(delegate);
    assertEquals(merged, chooser.choose(delegate, child));
    assertEquals(delegate, chooser.choose(delegate, null));
    assertEquals(child, chooser.choose(null, child));
    assertNull(chooser.choose(null, null));
  }

  @Test
  public void testChooseDelegateAndChildReportDelegateUsageWarning() {
    List<String> delegate = Collections.singletonList("delegateValue"), child =
        Arrays.asList("childValue0", "childValue1");
    Consumer<List<String>> reportDelegateUsageWarning = mock(Consumer.class);
    TransitionalIsolationListChooser<String> chooser =
        new TransitionalIsolationListChooser<>(reportDelegateUsageWarning);
    List<String> merged = new ArrayList<>(child);
    merged.addAll(delegate);
    assertEquals(merged, chooser.choose(delegate, child));
    verify(reportDelegateUsageWarning).accept(delegate);
  }

  @Test
  public void testChooseDelegateOnlyReportDelegateUsageWarning() {
    List<String> delegate = Collections.singletonList("delegateValue");
    Consumer<List<String>> reportDelegateUsageWarning = mock(Consumer.class);
    TransitionalIsolationListChooser<String> chooser =
        new TransitionalIsolationListChooser<>(reportDelegateUsageWarning);
    assertEquals(delegate, chooser.choose(delegate, null));
    verify(reportDelegateUsageWarning).accept(delegate);
  }
}
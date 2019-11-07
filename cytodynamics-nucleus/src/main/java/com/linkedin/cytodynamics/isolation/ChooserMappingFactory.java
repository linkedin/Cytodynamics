/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.isolation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import com.linkedin.cytodynamics.nucleus.IsolationLevel;


public class ChooserMappingFactory {
  public static <T> Map<IsolationLevel, Chooser<T>> buildChooserMapping(Consumer<T> reportDelegateUsageWarning) {
    Map<IsolationLevel, Chooser<T>> chooserMap = new HashMap<>();
    chooserMap.put(IsolationLevel.NONE, new NoneIsolationChooser<>());
    chooserMap.put(IsolationLevel.TRANSITIONAL, new TransitionalIsolationChooser<>(reportDelegateUsageWarning));
    chooserMap.put(IsolationLevel.FULL, new FullIsolationChooser<>());
    return Collections.unmodifiableMap(chooserMap);
  }

  public static <T> Map<IsolationLevel, Chooser<List<T>>> buildChooserMappingForList(Consumer<List<T>> reportDelegateUsageWarning) {
    Map<IsolationLevel, Chooser<List<T>>> chooserMap = new HashMap<>();
    chooserMap.put(IsolationLevel.NONE, new NoneIsolationListChooser<>());
    chooserMap.put(IsolationLevel.TRANSITIONAL, new TransitionalIsolationListChooser<>(reportDelegateUsageWarning));
    // can just use FullIsolationChooser for FULL since we don't need merging
    chooserMap.put(IsolationLevel.FULL, new FullIsolationChooser<>());
    return Collections.unmodifiableMap(chooserMap);
  }
}

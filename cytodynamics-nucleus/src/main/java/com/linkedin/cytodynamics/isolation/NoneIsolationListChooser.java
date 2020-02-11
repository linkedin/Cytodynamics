/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.isolation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A {@link NoneIsolationChooser} that works on {@link List}s. It merges child and delegate entries when both exist.
 */
public class NoneIsolationListChooser<T> extends NoneIsolationChooser<List<T>> {
  /**
   * Merge the child and the delegate, giving priority to the child elements.
   */
  @Override
  protected List<T> fromDelegateAndChild(List<T> delegate, List<T> child) {
    List<T> merged = new ArrayList<>(child);
    merged.addAll(delegate);
    return Collections.unmodifiableList(merged);
  }
}

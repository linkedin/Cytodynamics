/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.isolation;

import java.util.List;
import java.util.function.Consumer;


/**
 * A {@link TransitionalIsolationChooser} that works on {@link List}s. It merges child and delegate entries when both
 * exist.
 */
public class TransitionalIsolationListChooser<T> extends NoneIsolationListChooser<T> {
  private final Consumer<List<T>> reportDelegateUsageWarning;

  public TransitionalIsolationListChooser(Consumer<List<T>> reportDelegateUsageWarning) {
    this.reportDelegateUsageWarning = reportDelegateUsageWarning;
  }

  @Override
  protected List<T> fromDelegateAndChild(List<T> delegate, List<T> child) {
    this.reportDelegateUsageWarning.accept(delegate);
    return super.fromDelegateAndChild(delegate, child);
  }

  @Override
  protected List<T> fromDelegateOnly(List<T> delegate) {
    this.reportDelegateUsageWarning.accept(delegate);
    return super.fromDelegateOnly(delegate);
  }
}

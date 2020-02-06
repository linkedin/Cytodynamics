/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.isolation;

import java.util.function.Consumer;


public class TransitionalIsolationChooser<T> extends NoneIsolationChooser<T> {
  private final Consumer<T> reportDelegateUsageWarning;

  public TransitionalIsolationChooser(Consumer<T> reportDelegateUsageWarning) {
    this.reportDelegateUsageWarning = reportDelegateUsageWarning;
  }

  @Override
  protected T fromDelegateOnly(T delegate) {
    this.reportDelegateUsageWarning.accept(delegate);
    return super.fromDelegateOnly(delegate);
  }
}
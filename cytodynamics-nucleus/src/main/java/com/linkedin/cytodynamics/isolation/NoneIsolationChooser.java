/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.isolation;

public class NoneIsolationChooser<T> extends Chooser<T> {
  @Override
  protected T fromDelegateAndChild(T delegate, T child) {
    return child;
  }

  @Override
  protected T fromDelegateOnly(T delegate) {
    return delegate;
  }

  @Override
  protected T fromChildOnly(T child) {
    return child;
  }
}

/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

/**
 * Log API adapter, used to wrap over different logging implementations without having any external dependencies.
 */
class LogApiAdapter {
  private static LogAdapter impl;

  static {
    if (SLF4jLogAdapter.isAvailable()) {
      impl = new SLF4jLogAdapter();
    } else if (Log4j2LogAdapter.isAvailable()) {
      impl = new Log4j2LogAdapter();
    } else if (Log4j1LogAdapter.isAvailable()) {
      impl = new Log4j1LogAdapter();
    } else {
      impl = new JulLogAdapter();
    }
  }

  static Logger getLogger(Class<?> clazz) {
    return impl.getLogger(clazz.getName());
  }
}

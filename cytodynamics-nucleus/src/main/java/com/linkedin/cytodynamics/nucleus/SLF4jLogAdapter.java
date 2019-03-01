/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import org.slf4j.LoggerFactory;


/**
 * Logging adapter for SLF4j.
 */
class SLF4jLogAdapter implements LogAdapter {
  static boolean isAvailable() {
    try {
      Class<?> ignored = Class.forName("org.slf4j.Logger");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  static class SLF4JLogger implements Logger {
    private org.slf4j.Logger impl;

    @Override
    public void info(String message) {
      impl.info(message);
    }

    @Override
    public void warn(String message) {
      impl.warn(message);
    }

    @Override
    public void error(String message) {
      impl.error(message);
    }

    public SLF4JLogger(org.slf4j.Logger impl) {
      this.impl = impl;
    }
  }

  @Override
  public Logger getLogger(String loggerName) {
    return new SLF4JLogger(LoggerFactory.getLogger(loggerName));
  }
}

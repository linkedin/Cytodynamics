/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.util.logging.Level;


/**
 * Log adapter for java.util.logging.
 */
class JulLogAdapter implements LogAdapter {
  static class JulLogger implements Logger {
    private java.util.logging.Logger impl;

    @Override
    public void info(String message) {
      impl.log(Level.INFO, message);
    }

    @Override
    public void warn(String message) {
      impl.log(Level.WARNING, message);
    }

    @Override
    public void error(String message) {
      impl.log(Level.SEVERE, message);
    }

    public JulLogger(java.util.logging.Logger impl) {
      this.impl = impl;
    }
  }
  @Override
  public Logger getLogger(String loggerName) {
    return new JulLogger(java.util.logging.Logger.getLogger(loggerName));
  }
}

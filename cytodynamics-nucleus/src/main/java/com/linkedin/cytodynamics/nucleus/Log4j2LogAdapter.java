/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import org.apache.logging.log4j.LogManager;


/**
 * Log adapter for Log4j v2.
 */
class Log4j2LogAdapter implements LogAdapter {
  static boolean isAvailable() {
    try {
      Class.forName("org.apache.logging.log4j.Logger");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  static class Log4jLogger implements Logger {
    private org.apache.logging.log4j.Logger impl;

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

    public Log4jLogger(org.apache.logging.log4j.Logger impl) {
      this.impl = impl;
    }
  }

  @Override
  public Logger getLogger(String loggerName) {
    return new Log4jLogger(LogManager.getLogger(loggerName));
  }
}

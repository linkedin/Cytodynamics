/**
 * BSD 2-CLAUSE LICENSE
 *
 * Copyright 2018 LinkedIn Corporation.
 * All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.linkedin.cytodynamics.nucleus;

import org.apache.log4j.LogManager;


/**
 * Log adapter for log4j v1.
 */
class Log4j1LogAdapter implements LogAdapter {
  static boolean isAvailable() {
    try {
      Class<?> ignored = Class.forName("org.apache.log4j.Logger");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  static class Log4jLogger implements Logger {
    private org.apache.log4j.Logger impl;

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

    public Log4jLogger(org.apache.log4j.Logger impl) {
      this.impl = impl;
    }
  }

  @Override
  public Logger getLogger(String loggerName) {
    return new Log4jLogger(LogManager.getLogger(loggerName));
  }
}

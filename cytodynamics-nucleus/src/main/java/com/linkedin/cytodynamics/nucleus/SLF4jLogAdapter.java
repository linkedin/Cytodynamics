/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

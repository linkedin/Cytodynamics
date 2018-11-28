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

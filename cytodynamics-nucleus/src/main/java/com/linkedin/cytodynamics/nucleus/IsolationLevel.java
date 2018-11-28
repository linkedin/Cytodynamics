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
 * Levels of classpath isolation to use.
 */
public enum IsolationLevel {
  /**
   * No isolation from the parent classloader, all classes available in the parent "leak" into the child classloader.
   */
  NONE,
  /**
   * Transitional isolation, same as no isolation, but log which classes would not be found under full isolation.
   */
  TRANSITIONAL,
  /**
   * Complete isolation, classes that are not marked as part of a public API or not whitelisted are not available.
   */
  FULL
}

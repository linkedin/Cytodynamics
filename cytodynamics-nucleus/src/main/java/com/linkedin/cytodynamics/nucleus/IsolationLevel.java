/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
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

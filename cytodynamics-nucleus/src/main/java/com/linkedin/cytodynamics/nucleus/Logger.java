/**
 * Copyright 2018 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

/**
 * Interface for loggers.
 */
interface Logger {
  void info(String message);
  void warn(String message);
  void error(String message);
}

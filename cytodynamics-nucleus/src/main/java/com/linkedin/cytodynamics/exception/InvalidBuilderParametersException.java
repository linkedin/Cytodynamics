/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.exception;

public class InvalidBuilderParametersException extends RuntimeException {
  public InvalidBuilderParametersException(String message) {
    super(message);
  }

  public InvalidBuilderParametersException(String message, Throwable t) {
    super(message, t);
  }
}

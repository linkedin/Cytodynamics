/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.exception;

public class CytodynamicsClassNotFoundException extends ClassNotFoundException {
  public CytodynamicsClassNotFoundException(String message) {
    super(message);
  }
}

/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import com.linkedin.cytodynamics.exception.OriginValidationException;


/**
 * Origin matching results.
 */
enum OriginMatchResults {
  ALLOW(true, true),
  DENY(true, false),
  NO_MATCH(false, false);

  OriginMatchResults(boolean matches, boolean allowed) {
    this.matches = matches;
    this.allowed = allowed;
  }

  private final boolean matches;
  private final boolean allowed;

  public boolean matches() {
    return matches;
  }

  public boolean isAllowed() {
    if (!matches) {
      throw new OriginValidationException("Calling isAllowed() on a non-match");
    }

    return allowed;
  }}

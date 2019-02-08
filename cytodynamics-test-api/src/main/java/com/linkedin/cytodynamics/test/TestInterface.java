/**
 * Copyright 2018 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.test;

import com.linkedin.cytodynamics.nucleus.Api;


/**
 * An interface to be implemented by tests.
 */
@Api(name = "test-interface")
public interface TestInterface {
  String getValue();
  boolean classExists(String className);
}

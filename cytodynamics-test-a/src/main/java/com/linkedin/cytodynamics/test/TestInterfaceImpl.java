/**
 * Copyright 2018 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.test;

/**
 * An implementation of the test interface that returns "A".
 */
public class TestInterfaceImpl implements TestInterface {
  @Override
  public String getValue() {
    return "A";
  }

  @Override
  public boolean classExists(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}

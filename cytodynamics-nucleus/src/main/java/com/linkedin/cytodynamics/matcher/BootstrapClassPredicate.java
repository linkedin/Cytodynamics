/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.matcher;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Predicate;


/**
 * Checks if a class can be loaded by the bootstrap classloader.
 *
 * This predicate will try to load a class using the bootstrap classloader, so using this predicate will have the same
 * side effects as loading a class with the bootstrap classloader.
 */
public class BootstrapClassPredicate implements Predicate<String> {
  private final ClassLoader classLoaderForBootstrapClasses;

  public BootstrapClassPredicate() {
    /*
     * Null parent means to use the bootstrap classloader as the parent. Pass empty urls so that the only classes that
     * can be loaded are from the parent (i.e. bootstrap classloader).
     */
    this.classLoaderForBootstrapClasses = new URLClassLoader(new URL[]{}, null);
  }

  @Override
  public boolean test(String s) {
    try {
      this.classLoaderForBootstrapClasses.loadClass(s);
      // if no exception is thrown, then the class was loaded, so it is a bootstrap class
      return true;
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      return false;
    }
  }
}

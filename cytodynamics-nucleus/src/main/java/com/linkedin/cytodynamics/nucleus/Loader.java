/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

/**
 * Loader interface, which provides class loading services; instances of Loader can be obtained using
 * {@link LoaderBuilder}.
 *
 * Each instance of Loader has its own isolated classloader. This means that all instances of classes coming from a
 * single instance of a loader will share the same classloader; different instances of Loaders will each have their own
 * classloading context.
 */
public interface Loader {
  /**
   * Creates a new instance of a class using its default constructor.
   *
   * @param type The class of the expected type
   * @param className The name of the class to load
   * @param <T> The type that is expected
   * @return An instance of T
   */
  <T> T newInstanceOf(Class<T> type, String className);

  /**
   * Obtains an instance of a Class in this Loader's context.
   *
   * @param type The class of the expected type
   * @param className The name of the class to load
   * @param <T> The type that is expected
   * @return The Class instance for this type, as loaded using this Loader's context.
   */
  <T> Class<? extends T> loadClass(Class<T> type, String className);
}

/**
 * BSD 2-CLAUSE LICENSE
 *
 * Copyright 2018 LinkedIn Corporation.
 * All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

/*
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.isolation;

/**
 * Chooses a value based on the existence of a delegate entity and a child entity.
 * @param <T> type of the entity
 */
public abstract class Chooser<T> {
  /**
   * Choose a value from {@code delegate} and {@code child} based on existence of those arguments. An entity exists if
   * it is non-null, and it does not exist if it is null.
   * @param delegate value from delegate; null means that no value was found from the delegate
   * @param child value from child; null means that no value was found from the child
   * @return chosen value based on the delegate and child
   */
  public T choose(T delegate, T child) {
    if (delegate != null && child != null) {
      return fromDelegateAndChild(delegate, child);
    } else if (delegate != null) {
      // child would be null in this case
      return fromDelegateOnly(delegate);
    } else if (child != null) {
      // delegate would be null in this case
      return fromChildOnly(child);
    } else {
      // both delegate and child are null
      return null;
    }
  }

  /**
   * Choose based on both the delegate and child entity existing.
   */
  protected abstract T fromDelegateAndChild(T delegate, T child);

  /**
   * Choose based on only the delegate entity existing.
   */
  protected abstract T fromDelegateOnly(T delegate);

  /**
   * Choose based on only the child entity existing.
   */
  protected abstract T fromChildOnly(T child);
}

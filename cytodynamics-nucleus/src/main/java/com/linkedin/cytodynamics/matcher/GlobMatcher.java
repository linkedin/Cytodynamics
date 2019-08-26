/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.matcher;

import java.util.function.Predicate;
import java.util.regex.Pattern;


/**
 * Simple wrapper over regexes for glob matching.
 */
public class GlobMatcher implements Predicate<String> {
  private final Pattern pattern;

  public GlobMatcher(String globPattern) {
    // Turn the glob expression into a regex
    StringBuilder builder = new StringBuilder();
    char[] globPatternChars = globPattern.toCharArray();
    StringBuilder currentToken = new StringBuilder();

    for (char globPatternChar : globPatternChars) {
      switch(globPatternChar) {
        case '*':
          if (currentToken.length() != 0) {
            builder.append(Pattern.quote(currentToken.toString()));
            currentToken = new StringBuilder();
          }
          builder.append(".*");
          break;
        case '?':
          if (currentToken.length() != 0) {
            builder.append(Pattern.quote(currentToken.toString()));
            currentToken = new StringBuilder();
          }
          builder.append(".");
          break;
        default:
          currentToken.append(globPatternChar);
          break;
      }
    }

    if (currentToken.length() != 0) {
      builder.append(Pattern.quote(currentToken.toString()));
    }

    String regexPattern = builder.toString();
    pattern = Pattern.compile(regexPattern);
  }

  @Override
  public boolean test(String toMatch) {
    return pattern.matcher(toMatch).matches();
  }
}

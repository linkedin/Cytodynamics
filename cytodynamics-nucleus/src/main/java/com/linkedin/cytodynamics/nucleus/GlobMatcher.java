/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkedin.cytodynamics.nucleus;

import java.util.regex.Pattern;


/**
 * Simple wrapper over regexes for glob matching.
 */
class GlobMatcher {
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

  public boolean matches(String toMatch) {
    return pattern.matcher(toMatch).matches();
  }
}

/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.nucleus;

import java.net.URL;


/**
 * Origin filter interface.
 */
interface OriginRestrictionFilter {
  OriginMatchResults isAllowed(URL url);
}

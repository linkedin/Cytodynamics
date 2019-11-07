/**
 * Copyright 2018-2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").
 * See LICENSE in the project root for license information.
 */
package com.linkedin.cytodynamics.isolation;

import com.linkedin.cytodynamics.nucleus.IsolationLevel;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class TestChooserMappingFactory {
  @Test
  public void testBuildChooserMapping() {
    Map<IsolationLevel, Chooser<String>> chooserMappingFactory = ChooserMappingFactory.buildChooserMapping(obj -> {
    });
    assertTrue(chooserMappingFactory.get(IsolationLevel.NONE) instanceof NoneIsolationChooser);
    assertTrue(chooserMappingFactory.get(IsolationLevel.TRANSITIONAL) instanceof TransitionalIsolationChooser);
    assertTrue(chooserMappingFactory.get(IsolationLevel.FULL) instanceof FullIsolationChooser);
  }

  @Test
  public void testBuildChooserMappingForList() {
    Map<IsolationLevel, Chooser<List<String>>> chooserMappingFactory =
        ChooserMappingFactory.buildChooserMappingForList(obj -> {
        });
    assertTrue(chooserMappingFactory.get(IsolationLevel.NONE) instanceof NoneIsolationListChooser);
    assertTrue(chooserMappingFactory.get(IsolationLevel.TRANSITIONAL) instanceof TransitionalIsolationListChooser);
    assertTrue(chooserMappingFactory.get(IsolationLevel.FULL) instanceof FullIsolationChooser);
  }

  @Test(description = "Validates that all isolation levels are handled in the mapping built by the factory")
  public void testAllLevelsHandled() {
    Map<IsolationLevel, Chooser<String>> chooserMappingFactory = ChooserMappingFactory.buildChooserMapping(obj -> {
    });
    assertEquals(chooserMappingFactory.size(), IsolationLevel.values().length);

    Map<IsolationLevel, Chooser<List<String>>> chooserMappingFactoryForList =
        ChooserMappingFactory.buildChooserMappingForList(obj -> {
        });
    assertEquals(chooserMappingFactoryForList.size(), IsolationLevel.values().length);
  }
}
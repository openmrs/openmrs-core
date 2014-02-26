package org.openmrs.web.controller.concept;

import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptNumeric;
import org.openmrs.web.controller.ConceptFormController;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

import java.util.ArrayList;

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
public class ConceptFormControllerTest extends BaseWebContextSensitiveTest {
	
	@Test
	public void ConceptFormBackingObject_shouldCopyNumericAttributes() {
		ConceptNumeric concept = Mockito.mock(ConceptNumeric.class);
		Mockito.when(concept.isNumeric()).thenReturn(Boolean.TRUE);
		Mockito.when(concept.getHiAbsolute()).thenReturn(5.2);
		Mockito.when(concept.getLowAbsolute()).thenReturn(1.0);
		
		Mockito.when(concept.getHiCritical()).thenReturn(4.1);
		Mockito.when(concept.getLowCritical()).thenReturn(2.1);
		
		Mockito.when(concept.getLowNormal()).thenReturn(3.1);
		Mockito.when(concept.getHiNormal()).thenReturn(3.9);
		
		Mockito.when(concept.getPrecise()).thenReturn(Boolean.TRUE);
		Mockito.when(concept.getDisplayPrecision()).thenReturn(42);
		
		Mockito.when(concept.getUnits()).thenReturn("ml");
		
		Mockito.when(concept.getConceptMappings()).thenReturn(new ArrayList<ConceptMap>());
		
		ConceptFormController controller = new ConceptFormController();
		ConceptFormController.ConceptFormBackingObject conceptFormBackingObject = controller.new ConceptFormBackingObject(
		                                                                                                                  concept);
		
		org.junit.Assert.assertEquals(Double.valueOf(5.2), conceptFormBackingObject.getHiAbsolute());
		org.junit.Assert.assertEquals(Double.valueOf(1.0), conceptFormBackingObject.getLowAbsolute());
		
		org.junit.Assert.assertEquals(Double.valueOf(4.1), conceptFormBackingObject.getHiCritical());
		org.junit.Assert.assertEquals(Double.valueOf(2.1), conceptFormBackingObject.getLowCritical());
		
		org.junit.Assert.assertEquals(Double.valueOf(3.1), conceptFormBackingObject.getLowNormal());
		org.junit.Assert.assertEquals(Double.valueOf(3.9), conceptFormBackingObject.getHiNormal());
		
		org.junit.Assert.assertTrue(conceptFormBackingObject.isPrecise());
		org.junit.Assert.assertEquals(Integer.valueOf(42), conceptFormBackingObject.getDisplayPrecision());
		
		org.junit.Assert.assertEquals("ml", conceptFormBackingObject.getUnits());
	}
}

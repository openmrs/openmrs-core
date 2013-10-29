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
package org.openmrs;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests the {@link ConceptNumeric} object
 */
public class ConceptNumericTest extends BaseContextSensitiveTest {
	
	/**
	 * Regression test for TRUNK-82 (old TRAC-1511)
	 * 
	 * @see {@link ConceptNumeric#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not return true if obj is concept", method = "equals(Object)")
	public void equals_shouldNotReturnTrueIfObjIsConcept() throws Exception {
		ConceptNumeric cn = new ConceptNumeric(123);
		Concept c = new Concept(123);
		
		Assert.assertNotSame(c, cn);
		Assert.assertNotSame(cn, c);
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept concept)}
	 */
	@Test
	@Verifies(value = "should save a new concept numeric with a display precision field", method = "saveConcept(Concept concept)")
	public void saveConcept_shouldSaveANewConceptNumericWithADisplayPrecisionField() throws Exception {
		ConceptNumeric conceptNumeric = new ConceptNumeric(6324);
		Concept c = new Concept();
		
		c.addName(new ConceptName("testConceptName", Context.getLocale()));
		
		conceptNumeric.setDatatype(Context.getConceptService().getConceptDatatype(1));
		conceptNumeric.setDisplayPrecision(32);
		conceptNumeric.setNames(c.getNames());
		
		ConceptService conceptService = Context.getConceptService();
		
		conceptService.saveConcept(conceptNumeric);
		
		Assert.assertNotNull(conceptService.getConceptNumeric(6324).getDisplayPrecision());
	}
	
	/**
	 * @see {@link ConceptService#saveConcept(Concept concept)}
	 */
	@Test
	@Verifies(value = "should update an existing concept numeric with a display precision field", method = "saveConcept(Concept concept)")
	public void saveConcept_shouldUpDateAnExisitingConceptNumericWithADisplayPrecisionField() throws Exception {
		ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(5089);
		ConceptService conceptService = Context.getConceptService();
		int oldDisplayPrecision = conceptService.getConceptNumeric(5089).getDisplayPrecision();
		
		conceptNumeric.setDisplayPrecision(33);
		
		conceptService.saveConcept(conceptNumeric);
		
		int newDisplayPrecision = conceptService.getConceptNumeric(5089).getDisplayPrecision();
		
		Assert.assertNotNull(newDisplayPrecision);
		Assert.assertNotEquals(newDisplayPrecision, oldDisplayPrecision);
	}
}

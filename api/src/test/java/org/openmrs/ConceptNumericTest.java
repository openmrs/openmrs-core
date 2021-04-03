/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Tests the {@link ConceptNumeric} object
 */
public class ConceptNumericTest extends BaseContextSensitiveTest {
	
	/**
	 * Regression test for TRUNK-82 (old TRAC-1511)
	 * 
	 * @see ConceptNumeric#equals(Object)
	 */
	@Test
	public void equals_shouldNotReturnTrueIfObjIsConcept() {
		ConceptNumeric cn = new ConceptNumeric(123);
		Concept c = new Concept(123);
		
		assertNotSame(c, cn);
		assertNotSame(cn, c);
	}
	
	@Test
	public void equals_shouldNotBeTheSameReference() {
		Concept c = new Concept(123);
		ConceptNumeric cn = new ConceptNumeric(c);
		
		assertNotSame(c.getAnswers(), cn.getAnswers());
		assertNotSame(c.getConceptSets(), cn.getConceptSets());
		assertNotSame(cn.getConceptSets(), c.getConceptSets());
		assertNotSame(c.getNames(), cn.getNames());
		assertNotSame(c.getConceptMappings(), cn.getConceptMappings());
		assertNotSame(c.getDescriptions(), cn.getDescriptions());
	}
	
	@Test
	public void shouldChangeConceptAnswerReferenceToParentConcept() {
		Concept c = new Concept(123);
		c.addAnswer(new ConceptAnswer(1));
		c.addAnswer(new ConceptAnswer(2));
		ConceptNumeric cn = new ConceptNumeric(c);
		
		for (ConceptAnswer cAnswer : cn.getAnswers()) {
			assertSame(cn, cAnswer.getConcept());
		}
	}
	
	@Test
	public void shouldChangeConceptSetReferenceToParentConcept() {
		Concept c = new Concept(123);
		c.addSetMember(new Concept(1));
		c.addSetMember(new Concept(2));
		ConceptNumeric cn = new ConceptNumeric(c);
		
		for (ConceptSet cSet : cn.getConceptSets()) {
			assertSame(cn, cSet.getConceptSet());
		}
	}
	
	@Test
	public void shouldChangeConceptNameReferenceToParentConcept() {
		Concept c = new Concept(123);
		c.addName(new ConceptName(1));
		c.addName(new ConceptName(2));
		ConceptNumeric cn = new ConceptNumeric(c);
		
		for (ConceptName cName : cn.getNames()) {
			assertSame(cn, cName.getConcept());
		}
	}
	
	@Test
	public void shouldChangeConceptDescriptionReferenceToParentConcept() {
		Concept c = new Concept(123);
		c.addDescription(new ConceptDescription(1));
		c.addDescription(new ConceptDescription(2));
		ConceptNumeric cn = new ConceptNumeric(c);
		
		for (ConceptDescription cDesc : cn.getDescriptions()) {
			assertSame(cn, cDesc.getConcept());
		}
	}
	
	@Test
	public void shouldChangeConceptMapReferenceToParentConcept() {
		Concept c = new Concept(123);
		c.getConceptMappings().add(new ConceptMap(1));
		c.getConceptMappings().add(new ConceptMap(2));
		ConceptNumeric cn = new ConceptNumeric(c);
		
		for (ConceptMap cMap : cn.getConceptMappings()) {
			assertSame(cn, cMap.getConcept());
		}
	}
	
	/**
	 * Tests if {@link org.openmrs.api.ConceptService#saveConcept(Concept)} saves a ConceptNumeric with allowDecimal value
	 */
	@Test
	public void shouldSaveAConceptNumericWithAllowDecimalValue() {
		Concept c = Context.getConceptService().getConcept(22);
		ConceptNumeric cn = new ConceptNumeric(c);
		cn.addDescription(new ConceptDescription("some description", null));
		
		Context.getConceptService().saveConcept(cn);
		assertFalse(Context.getConceptService().getConceptNumeric(22).getAllowDecimal());
		
		cn.setAllowDecimal(true);
		Context.getConceptService().saveConcept(cn);
		assertTrue(Context.getConceptService().getConceptNumeric(22).getAllowDecimal());
	}
}

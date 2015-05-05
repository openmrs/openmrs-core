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

import org.junit.Assert;
import org.junit.Test;
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
	
	@Test
	@Verifies(value = "should make deep copie of collections", method = "ConceptNumeric(Concept)")
	public void equals_shouldNotBeTheSameReference() throws Exception {
		Concept c = new Concept(123);
		ConceptNumeric cn = new ConceptNumeric(c);
		
		Assert.assertNotSame(c.getAnswers(), cn.getAnswers());
		Assert.assertNotSame(c.getConceptSets(), cn.getConceptSets());
		Assert.assertNotSame(cn.getConceptSets(), c.getConceptSets());
		Assert.assertNotSame(c.getNames(), cn.getNames());
		Assert.assertNotSame(c.getConceptMappings(), cn.getConceptMappings());
		Assert.assertNotSame(c.getDescriptions(), cn.getDescriptions());
	}
	
	@Test
	@Verifies(value = "should change reference to the parent object for objects in answers collection", method = "ConceptNumeric(Concept)")
	public void shouldChangeConceptAnswerReferenceToParentConcept() throws Exception {
		Concept c = new Concept(123);
		c.addAnswer(new ConceptAnswer(1));
		c.addAnswer(new ConceptAnswer(2));
		ConceptNumeric cn = new ConceptNumeric(c);
		
		for (ConceptAnswer cAnswer : cn.getAnswers()) {
			Assert.assertSame(cn, cAnswer.getConcept());
		}
	}
	
	@Test
	@Verifies(value = "should change reference to the parent object for objects in conceptSets collection", method = "ConceptNumeric(Concept)")
	public void shouldChangeConceptSetReferenceToParentConcept() throws Exception {
		Concept c = new Concept(123);
		c.addSetMember(new Concept(1));
		c.addSetMember(new Concept(2));
		ConceptNumeric cn = new ConceptNumeric(c);
		
		for (ConceptSet cSet : cn.getConceptSets()) {
			Assert.assertSame(cn, cSet.getConceptSet());
		}
	}
	
	@Test
	@Verifies(value = "should change reference to the parent object  for objects in names collection", method = "ConceptNumeric(Concept)")
	public void shouldChangeConceptNameReferenceToParentConcept() throws Exception {
		Concept c = new Concept(123);
		c.addName(new ConceptName(1));
		c.addName(new ConceptName(2));
		ConceptNumeric cn = new ConceptNumeric(c);
		
		for (ConceptName cName : cn.getNames()) {
			Assert.assertSame(cn, cName.getConcept());
		}
	}
	
	@Test
	@Verifies(value = "should change reference to the parent object for objects in descriptions collection", method = "ConceptNumeric(Concept)")
	public void shouldChangeConceptDescriptionReferenceToParentConcept() throws Exception {
		Concept c = new Concept(123);
		c.addDescription(new ConceptDescription(1));
		c.addDescription(new ConceptDescription(2));
		ConceptNumeric cn = new ConceptNumeric(c);
		
		for (ConceptDescription cDesc : cn.getDescriptions()) {
			Assert.assertSame(cn, cDesc.getConcept());
		}
	}
	
	@Test
	@Verifies(value = "should change reference to the parent object for objects in conceptMappings collection", method = "ConceptNumeric(Concept)")
	public void shouldChangeConceptMapReferenceToParentConcept() throws Exception {
		Concept c = new Concept(123);
		c.getConceptMappings().add(new ConceptMap(1));
		c.getConceptMappings().add(new ConceptMap(2));
		ConceptNumeric cn = new ConceptNumeric(c);
		
		for (ConceptMap cMap : cn.getConceptMappings()) {
			Assert.assertSame(cn, cMap.getConcept());
		}
	}
	
	/**
	 * Tests if {@link org.openmrs.api.ConceptService#saveConcept(Concept)} saves a ConceptNumeric with allowDecimal value
	 */
	@Test
	@Verifies(method = "saveConcept(Concept)", value = "should save a conceptNumeric with allowDecimal value")
	public void shouldSaveAConceptNumericWithAllowDecimalValue() throws Exception {
		Concept c = Context.getConceptService().getConcept(22);
		ConceptNumeric cn = new ConceptNumeric(c);
		
		Context.getConceptService().saveConcept(cn);
		Assert.assertFalse(Context.getConceptService().getConceptNumeric(22).getAllowDecimal());
		
		cn.setAllowDecimal(true);
		Context.getConceptService().saveConcept(cn);
		Assert.assertTrue(Context.getConceptService().getConceptNumeric(22).getAllowDecimal());
	}
}

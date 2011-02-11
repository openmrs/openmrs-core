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
import org.openmrs.test.Verifies;

public class ConceptAnswerTest {
	
	/**
	 * Given a ConceptAnswer with a null drug and a ConceptAnswer with a non-null drug, the .equals
	 * should not say they are true. (Assumes the concepts are equals)
	 * 
	 * @see {@link ConceptAnswer#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not return true given an object with just a null drug answer", method = "equals(Object)")
	public void equals_shouldNotReturnTrueGivenAnObjectWithJustANullDrugAnswer() throws Exception {
		ConceptAnswer conceptOnlyAnswer = new ConceptAnswer();
		conceptOnlyAnswer.setConcept(new Concept(1));
		conceptOnlyAnswer.setAnswerConcept(new Concept(2));
		conceptOnlyAnswer.setAnswerDrug(null); // this has a null drug
		
		ConceptAnswer conceptWithDrugOnlyAnswer = new ConceptAnswer();
		conceptWithDrugOnlyAnswer.setConcept(new Concept(1));
		conceptWithDrugOnlyAnswer.setAnswerConcept(new Concept(2));
		conceptWithDrugOnlyAnswer.setAnswerDrug(new Drug(100)); // this has drug
		
		Assert.assertFalse(conceptOnlyAnswer.equals(conceptWithDrugOnlyAnswer));
		Assert.assertFalse(conceptWithDrugOnlyAnswer.equals(conceptOnlyAnswer));
	}
}

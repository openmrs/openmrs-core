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
package org.openmrs.propertyeditor;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 *
 */
public class ConceptAnswersEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link ConceptAnswersEditor#setAsText(String)}
	 */
	@Test
	@Verifies(value = "set the sort weights with the least possible changes", method = "setAsText(String)")
	public void setAsText_shouldSetTheSortWeightsWithTheLeastPossibleChanges() throws Exception {
		ConceptService service = Context.getConceptService();
		Concept c = service.getConcept(21);
		
		ConceptAnswersEditor editor = new ConceptAnswersEditor(c.getAnswers(true));
		editor.setAsText("22 7 8");
		
		ConceptAnswer ca1 = service.getConceptAnswer(1);//conceptId=7
		ConceptAnswer ca2 = service.getConceptAnswer(2);//conceptId=8
		ConceptAnswer ca3 = service.getConceptAnswer(3);//conceptId=22
		
		Concept cafter = service.getConcept(21);
		Assert.assertEquals(3, cafter.getAnswers(true).size());
		Assert.assertTrue(ca3.getSortWeight() < ca1.getSortWeight());
		Assert.assertTrue(ca1.getSortWeight() < ca2.getSortWeight());
	}
}

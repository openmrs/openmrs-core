/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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

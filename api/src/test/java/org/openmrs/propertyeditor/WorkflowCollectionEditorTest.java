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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Tests {@link WorkflowCollectionEditor}
 */
public class WorkflowCollectionEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see WorkflowCollectionEditor#setAsText(String)
	 * @verifies update workflows in program
	 */
	@Test
	public void setAsText_shouldUpdateWorkflowsInProgram() throws Exception {
		Program program = Context.getProgramWorkflowService().getProgram(1);
		WorkflowCollectionEditor editor = new WorkflowCollectionEditor();
		
		Assert.assertEquals(2, program.getWorkflows().size());
		
		editor.setAsText("1:3");
		
		Assert.assertEquals(1, program.getWorkflows().size());
		Assert.assertEquals(3, program.getWorkflows().iterator().next().getConcept().getConceptId().intValue());
		Assert.assertEquals(3, program.getAllWorkflows().size());
	}
	
}

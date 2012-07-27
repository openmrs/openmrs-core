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

/**
 * Tests methods in the {@link ProgramWorkflow} class
 */
public class ProgramWorkflowTest {
	
	/**
	 * @see {@link Program#equals(Object)}
	 */
	@Test
	public void equals_shouldReturnFalseIfProgramInstancesAreDifferentAndIdsAreNull() throws Exception {
		Program program1 = new Program();
		Program program2 = new Program();
		Assert.assertFalse(program1.equals(program2));
		Assert.assertTrue(program1.equals(program1));
	}
	
	/**
	 * @see {@link ProgramWorkflow#equals(Object)}
	 */
	@Test
	public void equals_shouldReturnFalseIfProgramWorkflowInstancesAreDifferentAndIdsAreNull() throws Exception {
		ProgramWorkflow workflow1 = new ProgramWorkflow();
		ProgramWorkflow workflow2 = new ProgramWorkflow();
		Assert.assertFalse(workflow1.equals(workflow2));
		Assert.assertTrue(workflow1.equals(workflow1));
	}
	
	/**
	 * @see {@link ProgramWorkflowState#equals(Object)}
	 */
	@Test
	public void equals_shouldReturnFalseIfProgramWorkflowStateInstancesAreDifferentAndIdsAreNull() throws Exception {
		ProgramWorkflowState state1 = new ProgramWorkflowState();
		ProgramWorkflowState state2 = new ProgramWorkflowState();
		Assert.assertFalse(state1.equals(state2));
		Assert.assertTrue(state1.equals(state1));
	}
	
	/**
	 * @see {@link PatientProgram#equals(Object)}
	 */
	@Test
	public void equals_shouldReturnFalseIfPatientProgramInstancesAreDifferentAndIsAreNull() throws Exception {
		PatientProgram p1 = new PatientProgram();
		PatientProgram p2 = new PatientProgram();
		Assert.assertFalse(p1.equals(p2));
		Assert.assertTrue(p1.equals(p1));
	}
	
	/**
	 * @see {@link PatientSate#equals(Object)}
	 */
	@Test
	public void equals_shouldReturnFalseIfPatientStateInstancesAreDifferentAndIdsAreNull() throws Exception {
		PatientState state1 = new PatientState();
		PatientState state2 = new PatientState();
		Assert.assertFalse(state1.equals(state2));
		Assert.assertTrue(state1.equals(state1));
	}
	
	/**
	 * @see {@link ConceptStateConversion#equals(Object)}
	 */
	@Test
	public void equals_shouldReturnFalseIfConceptStateConversionInstancesAreDifferentAndIdsAreNull() throws Exception {
		ConceptStateConversion c1 = new ConceptStateConversion();
		ConceptStateConversion c2 = new ConceptStateConversion();
		Assert.assertFalse(c1.equals(c2));
		Assert.assertTrue(c1.equals(c1));
	}
}

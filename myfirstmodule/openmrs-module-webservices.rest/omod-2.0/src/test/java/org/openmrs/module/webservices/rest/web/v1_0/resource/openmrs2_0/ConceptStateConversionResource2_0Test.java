/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0;

import org.openmrs.Concept;
import org.openmrs.ConceptStateConversion;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ConceptStateConversionResource2_0Test
		extends BaseDelegatingResourceTest<ConceptStateConversionResource2_0, ConceptStateConversion> {

	@Override
	public ConceptStateConversion newObject() {
		Concept concept = Context.getConceptService().getConceptByUuid(RestTestConstants1_8.CONCEPT2_UUID);
		ProgramWorkflow workflow = Context.getProgramWorkflowService().getWorkflowByUuid(RestTestConstants1_8.WORKFLOW_UUID);

		ProgramWorkflowState state = new ProgramWorkflowState();
		state.setConcept(concept);
		state.setInitial(true);
		state.setTerminal(false);

		workflow.addState(state);
		Context.getProgramWorkflowService().saveProgram(workflow.getProgram());

		ConceptStateConversion conceptStateConversion = new ConceptStateConversion();
		conceptStateConversion.setConcept(concept);
		conceptStateConversion.setProgramWorkflow(workflow);
		conceptStateConversion.setProgramWorkflowState(state);
		conceptStateConversion.setUuid("e4f2fc12-0ca5-47d2-91bb-a129dca3149b");

		return conceptStateConversion;
	}

	@Override
	public String getDisplayProperty() {
		return "ConceptStateConversion: Concept[Concept #16] results in State [State DIED initial=true terminal=false] for workflow [ProgramWorkflow(id=1)]";
	}

	@Override
	public String getUuidProperty() {
		return "e4f2fc12-0ca5-47d2-91bb-a129dca3149b";
	}

	@Override
	public void validateRefRepresentation() throws Exception {
		assertPropPresent("concept");
		assertPropPresent("programWorkflow");
		assertPropPresent("programWorkflowState");
		super.validateRefRepresentation();
	}

	@Override
	public void validateDefaultRepresentation() throws Exception {
		assertPropPresent("concept");
		assertPropPresent("programWorkflow");
		assertPropPresent("programWorkflowState");
		super.validateDefaultRepresentation();
	}

	@Override
	public void validateFullRepresentation() throws Exception {
		assertPropPresent("concept");
		assertPropPresent("programWorkflow");
		assertPropPresent("programWorkflowState");
		super.validateFullRepresentation();
	}
}

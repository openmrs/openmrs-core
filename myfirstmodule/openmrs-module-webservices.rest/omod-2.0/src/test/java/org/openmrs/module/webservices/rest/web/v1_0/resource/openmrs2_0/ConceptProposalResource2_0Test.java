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

import org.junit.Before;
import org.openmrs.ConceptProposal;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.util.OpenmrsConstants;

public class ConceptProposalResource2_0Test extends BaseDelegatingResourceTest<ConceptProposalResource2_0, ConceptProposal> {

	private static ConceptProposal proposal;

	@Before
	public void before() throws Exception {
		proposal = new ConceptProposal();
		proposal.setOriginalText("original text");
		proposal.setFinalText("final text");
		proposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED);
		proposal.setComments("comments");
		proposal.setMappedConcept(Context.getConceptService().getConceptByUuid(RestTestConstants1_8.CONCEPT_UUID));
		Context.getConceptService().saveConceptProposal(proposal);
	}

	@Override
	public ConceptProposal newObject() {
		return proposal;
	}

	@Override
	public String getDisplayProperty() {
		return proposal.toString();
	}

	@Override
	public String getUuidProperty() {
		return proposal.getUuid();
	}

	@Override
	public void validateRefRepresentation() throws Exception {
		super.validateRefRepresentation();
		assertPropPresent("uuid");
		assertPropEquals("display", proposal.toString());
		assertPropPresent("encounter");
		assertPropEquals("originalText", proposal.getOriginalText());
		assertPropEquals("state", proposal.getState());
		assertPropEquals("occurrences", 1);
		assertPropPresent("creator");
		assertPropPresent("dateCreated");
	}

	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("uuid");
		assertPropEquals("display", proposal.toString());
		assertPropPresent("encounter");
		assertPropEquals("originalText", proposal.getOriginalText());
		assertPropEquals("finalText", proposal.getFinalText());
		assertPropEquals("state", proposal.getState());
		assertPropEquals("comments", proposal.getComments());
		assertPropEquals("occurrences", 1);
		assertPropPresent("creator");
		assertPropPresent("dateCreated");
	}

	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("uuid");
		assertPropEquals("display", proposal.toString());
		assertPropPresent("encounter");
		assertPropPresent("obsConcept");
		assertPropPresent("obs");
		assertPropPresent("mappedConcept");
		assertPropEquals("originalText", proposal.getOriginalText());
		assertPropEquals("finalText", proposal.getFinalText());
		assertPropEquals("state", proposal.getState());
		assertPropEquals("comments", proposal.getComments());
		assertPropEquals("occurrences", 1);
		assertPropPresent("creator");
		assertPropPresent("dateCreated");
	}
}

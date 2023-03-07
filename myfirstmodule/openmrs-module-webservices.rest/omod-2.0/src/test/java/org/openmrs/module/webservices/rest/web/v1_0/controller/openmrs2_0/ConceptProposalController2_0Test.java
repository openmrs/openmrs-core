/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptProposal;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.util.OpenmrsConstants;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConceptProposalController2_0Test extends MainResourceControllerTest {

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
	public String getURI() {
		return "conceptproposal";
	}

	@Override
	public String getUuid() {
		return proposal.getUuid();
	}

	@Override
	public long getAllCount() {
		return 2;
	}

	@Test
	public void shouldIgnoreProposal() throws Exception {
		ConceptProposal existingProposal = Context.getConceptService().getConceptProposalByUuid(proposal.getUuid());
		assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED, existingProposal.getState());

		String json = "{\"action\": \"ignore\"}";

		handle(newPostRequest(getURI() + "/" + proposal.getUuid(), json));

		ConceptProposal ignoredProposal = Context.getConceptService().getConceptProposalByUuid(proposal.getUuid());
		assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT, ignoredProposal.getState());
	}

	@Test
	public void shouldPurgeProposal() throws Exception {
		handle(newDeleteRequest(getURI() + "/" + proposal.getUuid(), new Parameter("purge", "true")));

		ConceptProposal purgedProposal = Context.getConceptService().getConceptProposalByUuid(proposal.getUuid());
		assertNull(purgedProposal);
	}

	@Test
	public void shouldUpdateProposal() throws Exception {
		String json = "{\"mappedConcept\": \"" + RestTestConstants1_8.CONCEPT2_UUID + "\"}";

		handle(newPostRequest(getURI() + "/" + proposal.getUuid(), json));

		ConceptProposal mappedProposal = Context.getConceptService().getConceptProposalByUuid(proposal.getUuid());
		assertEquals(RestTestConstants1_8.CONCEPT2_UUID, mappedProposal.getMappedConcept().getUuid());
	}

	@Test
	public void shouldSaveAsMapped() throws Exception {
		String json = "{\"action\": \"convert\",\"actionToTakeOnConvert\": \"saveAsMapped\",\"conceptNameLocale\": \"en\"}";

		handle(newPostRequest(getURI() + "/" + proposal.getUuid(), json));

		ConceptProposal mappedProposal = Context.getConceptService().getConceptProposalByUuid(proposal.getUuid());
		assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT, mappedProposal.getState());
	}

	@Test
	public void shouldSaveAsSynonym() throws Exception {
		String json = "{\"action\": \"convert\",\"actionToTakeOnConvert\": \"saveAsSynonym\",\"conceptNameLocale\": \"en\"}";

		handle(newPostRequest(getURI() + "/" + proposal.getUuid(), json));

		// assert new state
		ConceptProposal mappedProposal = Context.getConceptService().getConceptProposalByUuid(proposal.getUuid());
		assertEquals(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM, mappedProposal.getState());

		// assert new concept name
		Concept mappedConcept = Context.getConceptService().getConceptByUuid(mappedProposal.getMappedConcept().getUuid());
		Collection<ConceptName> mappedConceptNames = mappedConcept.getNames();
		assertTrue(mappedConceptNames.stream().anyMatch(n -> n.getName().equals(mappedProposal.getFinalText())));
	}
}

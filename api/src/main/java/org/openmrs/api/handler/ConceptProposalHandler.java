/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import java.util.Date;

import org.openmrs.ConceptProposal;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.util.OpenmrsConstants;

/**
 * This class deals with {@link ConceptProposal} objects when they are saved via a save* method in
 * an Openmrs Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP
 * class. <br/>
 * <br/>
 */
@Handler(supports = ConceptProposal.class)
public class ConceptProposalHandler implements SaveHandler<ConceptProposal> {
	
	/**
	 * @see org.openmrs.api.handler.SaveHandler#handle(org.openmrs.OpenmrsObject, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 */
	public void handle(ConceptProposal cp, User creator, Date dateCreated, String other) {
		if (cp.getState() == null) {
			cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED);
		}
		
		// set the creator and date created
		if (cp.getCreator() == null && cp.getEncounter() != null) {
			cp.setCreator(cp.getEncounter().getCreator());
		} else {
			cp.setCreator(creator);
		}
		
		if (cp.getDateCreated() == null && cp.getEncounter() != null) {
			cp.setDateCreated(cp.getEncounter().getDateCreated());
		} else {
			cp.setDateCreated(dateCreated);
		}
	}
}

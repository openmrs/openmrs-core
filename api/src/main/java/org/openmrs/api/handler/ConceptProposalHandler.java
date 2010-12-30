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

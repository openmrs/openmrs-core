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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;

/**
 *
 */
@Handler(supports = Encounter.class)
public class EncounterSaveHandler implements SaveHandler<Encounter> {
	
	private static final Log log = LogFactory.getLog(EncounterSaveHandler.class);
	
	/**
	 * @see org.openmrs.api.handler.SaveHandler#handle(org.openmrs.OpenmrsObject, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 */
	@Override
	public void handle(Encounter object, User creator, Date dateCreated, String other) {
		
		for (Obs obs : object.getAllObs()) {
			try {
				if (obs.getConcept().getDatatype().isComplex()) {
					String handlerString = Context.getConceptService().getConceptComplex(obs.getConcept().getConceptId())
					        .getHandler();
					Context.getObsService().getHandler(handlerString).saveObs(obs);
					
				}
			}
			catch (Exception e) {
				log.error("Unable to save complex obs", e);
			}
			
		}
	}
}

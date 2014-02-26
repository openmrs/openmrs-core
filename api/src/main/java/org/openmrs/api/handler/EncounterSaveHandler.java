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

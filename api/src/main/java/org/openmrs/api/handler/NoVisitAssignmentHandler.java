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

import java.util.Locale;

import org.openmrs.Encounter;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;

/**
 * This handler doesn't ever assign an encounter to a visit.
 * 
 * @see EncounterVisitHandler
 */
@Handler
public class NoVisitAssignmentHandler extends BaseEncounterVisitHandler {
	
	@Override
	public String getDisplayName(Locale locale) {
		return Context.getMessageSourceService().getMessage("Visit.assignmentHandler.noAssignment", null, locale);
	}
	
	@Override
	public void beforeCreateEncounter(Encounter encounter) {
		// not doing anything here. This is the simplest handler you can get
	}
	
}

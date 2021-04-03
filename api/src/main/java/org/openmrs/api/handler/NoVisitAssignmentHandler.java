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

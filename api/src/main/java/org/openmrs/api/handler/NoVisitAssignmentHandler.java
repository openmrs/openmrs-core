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

import java.util.List;
import java.util.Locale;

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;

/**
 * This handler doesn't ever assign an encounter to a visit. 
 * 
 * @see EncounterToVisitAssignmentHandler
 */
@Handler
public class NoVisitAssignmentHandler implements
		EncounterToVisitAssignmentHandler {

	@Override
	public String getDisplayName(Locale locale) {
		return Context.getMessageSourceService().getMessage(
				"visit.assignmentHandler.noAssignment", null, locale);
	}

	@Override
	public Visit getVisitForEncounter(List<Visit> activeVisits,
			Encounter encounter) {

		// a null return value means "don't associate this to anything"
		return null;
	}

}

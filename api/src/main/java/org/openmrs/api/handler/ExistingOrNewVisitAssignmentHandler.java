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
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

/**
 * This handler assigns an encounter to an existing visit, where appropriate, or creates a new one.
 * 
 * @see EncounterVisitHandler
 */
public class ExistingOrNewVisitAssignmentHandler extends ExistingVisitAssignmentHandler {
	
	/**
	 * @see org.openmrs.api.handler.ExistingVisitAssignmentHandler#getDisplayName(java.util.Locale)
	 */
	@Override
	public String getDisplayName(Locale locale) {
		return Context.getMessageSourceService().getMessage("visit.assignmentHandler.assignToExistingVisitOrNew", null,
		    locale);
	}
	
	/**
	 * @see org.openmrs.api.handler.ExistingVisitAssignmentHandler#beforeCreateEncounter(org.openmrs.Encounter)
	 * 
	 * @should assign existing visit if match found
	 * @should assign new visit if no match found
	 */
	@Override
	public void beforeCreateEncounter(Encounter encounter) {
		
		//Do the default assignment to an existing visit.
		super.beforeCreateEncounter(encounter);
		
		//Do nothing if the encounter already belongs to a visit.
		if (encounter.getVisit() != null)
			return;
		
		Visit visit = new Visit();
		visit.setStartDatetime(encounter.getEncounterDatetime());
		visit.setLocation(encounter.getLocation());
		visit.setPatient(encounter.getPatient());
		
		//TODO Is is correct?
		visit.setVisitType(Context.getVisitService().getAllVisitTypes().get(0));
		
		//set stop date time to last minute of the encounter day.
		visit.setStopDatetime(OpenmrsUtil.getLastMillisecondOfDay(encounter.getEncounterDatetime()));
		
		encounter.setVisit(visit);
	}
}

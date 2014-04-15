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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;

/**
 * This handler assigns an encounter to an existing visit, where appropriate, but will never create
 * a new visit.
 *
 * @see EncounterVisitHandler
 */
@Handler
public class ExistingVisitAssignmentHandler extends BaseEncounterVisitHandler {
	
	/**
	 * @see org.openmrs.api.handler.EncounterVisitHandler#getDisplayName(java.util.Locale)
	 */
	@Override
	public String getDisplayName(Locale locale) {
		return Context.getMessageSourceService().getMessage("visit.assignmentHandler.assignToExistingVisitOnly", null,
		    locale);
	}
	
	/**
	 * @see org.openmrs.api.handler.EncounterVisitHandler#beforeCreateEncounter(org.openmrs.Encounter)
	 * @should assign existing visit if match found
	 * @should not assign visit if no match found
	 * @should not assign visit which stopped before encounter date
	 */
	@Override
	public void beforeCreateEncounter(Encounter encounter) {
		
		//Do nothing if the encounter already belongs to a visit.
		if (encounter.getVisit() != null) {
			return;
		}
		
		List<Patient> patients = new ArrayList<Patient>();
		patients.add(encounter.getPatient());
		
		//Fetch visits for this patient that haven't ended by the encounter date.
		List<Visit> visits = Context.getVisitService().getVisits(null, patients, null, null, null,
		    encounter.getEncounterDatetime(), null, null, null, true, false);
		
		if (visits == null) {
			return;
		}
		
		Date encounterDate = encounter.getEncounterDatetime();
		
		for (Visit visit : visits) {
			//skip visits which are started after the encounter date.
			if (visit.getStartDatetime().after(encounterDate)) {
				continue;
			}
			
			//skip visits which ended before the encounter date
			if (visit.getStopDatetime() != null && visit.getStopDatetime().before(encounterDate)) {
				continue;
			}
			
			if (visit.getLocation() == null || Location.isInHierarchy(encounter.getLocation(), visit.getLocation())) {
				encounter.setVisit(visit);
				return;
			}
		}
	}
}

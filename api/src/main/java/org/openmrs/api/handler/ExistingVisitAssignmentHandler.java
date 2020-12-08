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
	 * <strong>Should</strong> assign existing visit if match found
	 * <strong>Should</strong> not assign visit if no match found
	 * <strong>Should</strong> not assign visit which stopped before encounter date
	 */
	@Override
	public void beforeCreateEncounter(Encounter encounter) {
		
		//Do nothing if the encounter already belongs to a visit.
		if (encounter.getVisit() != null) {
			return;
		}
		
		List<Patient> patients = new ArrayList<>();
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

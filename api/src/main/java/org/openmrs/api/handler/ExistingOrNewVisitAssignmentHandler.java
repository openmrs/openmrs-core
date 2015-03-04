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

import org.apache.commons.lang.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
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
		
		visit.setVisitType(getVisitType(encounter));
		
		//set stop date time to last millisecond of the encounter day.
		visit.setStopDatetime(OpenmrsUtil.getLastMomentOfDay(encounter.getEncounterDatetime()));
		
		encounter.setVisit(visit);
	}
	
	/**
	 * Gets the visit type for an encounter.
	 * 
	 * @param encounterType the encounter.
	 * @return the visit type for the encounter.
	 */
	protected VisitType getVisitType(Encounter encounter) throws APIException {
		
		//TODO this GP should be parsed and cached (as a map) instead of fetching it and parsing it every time. 
		//(And we should have a global property change listener to recalculate it when that GP is changed.)
		String value = Context.getAdministrationService().getGlobalPropertyValue(
		    OpenmrsConstants.GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING, "");
		
		//Value should be in this format "3:4, 5:2, 1:2, 2:2" for encounterTypeId:visitTypeId
		if (!StringUtils.isBlank(value)) {
			String targetEncounterTypeId = encounter.getEncounterType().getId().toString();
			
			String[] mappings = value.split(",");
			for (String mapping : mappings) {
				int index = mapping.indexOf(':');
				if (index > 0) {
					String encounterTypeId = mapping.substring(0, index).trim();
					if (targetEncounterTypeId.equals(encounterTypeId)) {
						String visitTypeId = mapping.substring(index + 1).trim();
						VisitType visitType = Context.getVisitService().getVisitType(Integer.parseInt(visitTypeId));
						if (visitType != null) {
							return visitType;
						}
					}
				}
			}
			
			//Reaching here means this encounter type is not in the user's mapping.
			throw new APIException(
			        "Global Property: visit.encounterTypeToVisitTypeMapping does not have a mapping for encounter type: "
			                + encounter.getEncounterType().getName());
		}
		
		return Context.getVisitService().getAllVisitTypes().get(0);
	}
}

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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * This handler assigns an encounter to an existing visit, where appropriate, or creates a new one.
 *
 * @see EncounterVisitHandler
 */
public class ExistingOrNewVisitAssignmentHandler extends ExistingVisitAssignmentHandler implements GlobalPropertyListener {
	
	private static volatile Map<EncounterType, VisitType> encounterVisitMapping;
	
	private static void setEncounterVisitMapping(Map<EncounterType, VisitType> encounterVisitMapping) {
		ExistingOrNewVisitAssignmentHandler.encounterVisitMapping = encounterVisitMapping;
	}
	
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
	 * @should assign existing visit if match found
	 * @should assign new visit if no match found
	 * @should resolve encounter and visit type uuids as global property values
	 */
	@Override
	public void beforeCreateEncounter(Encounter encounter) {
		
		//Do the default assignment to an existing visit.
		super.beforeCreateEncounter(encounter);
		
		//Do nothing if the encounter already belongs to a visit.
		if (encounter.getVisit() != null) {
			return;
		}
		
		Visit visit = new Visit();
		visit.setStartDatetime(encounter.getEncounterDatetime());
		visit.setLocation(encounter.getLocation());
		visit.setPatient(encounter.getPatient());
		
		if (encounterVisitMapping == null) {
			//initial one-time setup
			setEncounterVisitMapping(new HashMap<EncounterType, VisitType>());
			Context.getAdministrationService().addGlobalPropertyListener(this);
		}
		
		VisitType visitType = encounterVisitMapping.get(encounter.getEncounterType());
		if (visitType == null) {
			visitType = loadVisitType(encounter.getEncounterType());
			
			//replace reference instead of synchronizing
			Map<EncounterType, VisitType> newMap = new HashMap<EncounterType, VisitType>(encounterVisitMapping);
			newMap.put(encounter.getEncounterType(), visitType);
			
			setEncounterVisitMapping(newMap);
		}
		visit.setVisitType(visitType);
		
		//set stop date time to last millisecond of the encounter day.
		visit.setStopDatetime(OpenmrsUtil.getLastMomentOfDay(encounter.getEncounterDatetime()));
		
		encounter.setVisit(visit);
	}
	
	/**
	 * Get the visit type corresponding to an encounter type by reading valid mappings 
	 * from a global property
	 * @param encounterType
	 * @return
	 * @throws APIException
	 */
	private static VisitType loadVisitType(EncounterType encounterType) throws APIException {
		
		String value = Context.getAdministrationService().getGlobalPropertyValue(
		    OpenmrsConstants.GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING, "");
		
		// Value should be in this format "3:4, 5:2, 1:2, 2:2" for encounterTypeId:visitTypeId
		// or encounterTypeUuid:visitTypeUuid o a mixture of uuids and id
		if (!StringUtils.isBlank(value)) {
			
			VisitService visitService = Context.getVisitService();
			String targetEncounterTypeId = encounterType.getId().toString();
			
			String[] mappings = value.split(",");
			for (String mapping : mappings) {
				int index = mapping.indexOf(':');
				if (index > 0) {
					String encounterTypeIdOrUuid = mapping.substring(0, index).trim();
					if (targetEncounterTypeId.equals(encounterTypeIdOrUuid)
					        || encounterType.getUuid().equals(encounterTypeIdOrUuid)) {
						String visitTypeIdOrUuid = mapping.substring(index + 1).trim();
						VisitType visitType = null;
						if (StringUtils.isNumeric(visitTypeIdOrUuid)) {
							visitType = visitService.getVisitType(Integer.parseInt(visitTypeIdOrUuid));
						} else {
							visitType = visitService.getVisitTypeByUuid(visitTypeIdOrUuid);
						}
						if (visitType != null) {
							return visitType;
						}
					}
				}
			}
			
			// Reaching here means this encounter type is not in the user's mapping.
			throw new APIException("GlobalProperty.error.loadVisitType", new Object[] { encounterType.getName() });
		}
		
		return Context.getVisitService().getAllVisitTypes().get(0);
	}
	
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GP_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAPPING.equals(propertyName);
	}
	
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		setEncounterVisitMapping(new HashMap<EncounterType, VisitType>());
	}
	
	@Override
	public void globalPropertyDeleted(String propertyName) {
		setEncounterVisitMapping(new HashMap<EncounterType, VisitType>());
	}
	
}

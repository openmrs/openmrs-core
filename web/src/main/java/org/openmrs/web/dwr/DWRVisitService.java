/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;

/**
 * Contains methods for processing DWR requests for visits
 * 
 * @since 1.9
 */
public class DWRVisitService {
	
	private static final Log log = LogFactory.getLog(DWRVisitService.class);
	
	/**
	 * Gets all visits for the patient matching the given patientId
	 * 
	 * @param patientId the patient id for the patient whose visits to find
	 * @param includeInactive specifies if ended visits should be returned or not
	 * @param includeVoided specifies if voided visits should be returned or not
	 * @return a list of visit list items
	 * @see VisitListItem
	 * @throws APIException
	 */
	public Vector<Object> findVisitsByPatient(Integer patientId, boolean includeInactive, boolean includeVoided)
	        throws APIException {
		// List to return
		Vector<Object> objectList = new Vector<Object>();
		MessageSourceService mss = Context.getMessageSourceService();
		
		try {
			List<Visit> visits = new Vector<Visit>();
			
			if (patientId != null) {
				Patient p = Context.getPatientService().getPatient(patientId);
				if (p != null) {
					visits = Context.getVisitService().getVisitsByPatient(p, includeInactive, includeVoided);
				}
			} else {
				throw new APIException("errors.patientId.cannotBeNull", (Object[]) null);
			}
			
			if (visits.size() > 0) {
				objectList = new Vector<Object>(visits.size());
				for (Visit v : visits) {
					objectList.add(new VisitListItem(v));
				}
			}
		}
		catch (Exception e) {
			log.error("Error while searching for visits", e);
			objectList.add(mss.getMessage("Visit.search.error"));
		}
		return objectList;
	}
	
	/**
	 * Gets the visit matching the specified visitId
	 * 
	 * @param visitId the visit id to search against
	 * @return the {@link VisitListItem} for the matching visit
	 * @throws APIException
	 */
	public VisitListItem getVisit(Integer visitId) throws APIException {
		Visit v = Context.getVisitService().getVisit(visitId);
		return v == null ? null : new VisitListItem(v);
	}
	
	/**
	 * Fetches all encounters belonging to the visit that matches the specified visitId
	 * 
	 * @param visitId
	 * @return
	 * @throws APIException
	 */
	public Vector<Object> findEncountersByVisit(Integer visitId) throws APIException {
		// List to return
		Vector<Object> objectList = new Vector<Object>();
		
		try {
			List<Encounter> encounters = new Vector<Encounter>();
			
			if (visitId != null) {
				Visit v = Context.getVisitService().getVisit(visitId);
				if (v != null) {
					encounters = Context.getEncounterService().getEncountersByVisit(v, false);
				}
			} else {
				throw new APIException("VisitId.cannotBeNull", (Object[]) null);
			}
			
			if (encounters.size() > 0) {
				objectList = new Vector<Object>(encounters.size());
				for (Encounter e : encounters) {
					objectList.add(new EncounterListItem(e));
				}
			}
		}
		catch (Exception e) {
			log.warn("Error while finding encounters for the visit with id:" + visitId, e);
			objectList.add(Context.getMessageSourceService().getMessage("Visit.find.encounters.error"));
		}
		return objectList;
	}
	
}

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
package org.openmrs.web.dwr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;

public class DWRVisitService {
	
	private static final Log log = LogFactory.getLog(DWRVisitService.class);
	
	/**
	 * Gets all visits for the patient matching the given patientId
	 * 
	 * @param patientId the patient id for the patient whose visits to find
	 * @param includeEnded specifies if ended visits should be returned or not
	 * @param includeVoided specifies if voided visits should be returned or not
	 * @return a list of visit list items
	 * @see VisitListItem
	 * @throws APIException
	 */
	public Vector<Object> findVisitsByPatient(Integer patientId, boolean includeEnded, boolean includeVoided)
	        throws APIException {
		// List to return
		Vector<Object> objectList = new Vector<Object>();
		MessageSourceService mss = Context.getMessageSourceService();
		
		try {
			List<Visit> visits = new Vector<Visit>();
			
			if (patientId != null) {
				Patient p = Context.getPatientService().getPatient(patientId);
				if (p != null)
					visits = Context.getVisitService().getActiveVisitsByPatient(p);
			}
			
			if (visits.size() > 0) {
				objectList = new Vector<Object>(visits.size());
				for (Visit v : visits)
					objectList.add(new VisitListItem(v));
			}
		}
		catch (Exception e) {
			log.error("Error while searching for visits", e);
			objectList.add(mss.getMessage("Visit.search.error") + " - " + e.getMessage());
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
}

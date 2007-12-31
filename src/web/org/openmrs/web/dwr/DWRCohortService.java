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

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

/**
 *
 */
public class DWRCohortService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public void addPatientToCohort(Integer cohortId, Integer patientId) {
		Patient p = Context.getPatientService().getPatient(patientId);
		Cohort c = Context.getCohortService().getCohort(cohortId);
		Context.getCohortService().addPatientToCohort(c, p);
	}
	
	public void removePatientFromCohort(Integer cohortId, Integer patientId) {
		Patient p = Context.getPatientService().getPatient(patientId);
		Cohort c = Context.getCohortService().getCohort(cohortId);
		Context.getCohortService().removePatientFromCohort(c, p);
	}

	public Vector<ListItem> getCohorts() {
		Vector<ListItem> ret = new Vector<ListItem>();
		for (Cohort c : Context.getCohortService().getCohorts()) {
			ret.add(new ListItem(c.getCohortId(), c.getName(), c.getDescription()));
		}
		return ret;
	}
	
	public Vector<ListItem> getCohortsContainingPatient(Integer patientId) {
		Vector<ListItem> ret = new Vector<ListItem>();
		for (Cohort c : Context.getCohortService().getCohortsContainingPatientId(patientId)) {
			ret.add(new ListItem(c.getCohortId(), c.getName(), c.getDescription()));
		}
		return ret;
	}

}

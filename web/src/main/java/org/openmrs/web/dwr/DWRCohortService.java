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

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

/**
 * This class exposes some of the methods in {@link org.openmrs.api.CohortService} via the dwr
 * package
 */
public class DWRCohortService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Adds the {@link Patient} identified by <code>patientId</code> to the {@link Cohort}
	 * identified by <code>cohortId</code>
	 * 
	 * @param cohortId - Identifies the {@link Cohort} to add to
	 * @param patientId - Identifies the {@link Patient} to add
	 */
	public void addPatientToCohort(Integer cohortId, Integer patientId) {
		Patient p = Context.getPatientService().getPatient(patientId);
		Cohort c = Context.getCohortService().getCohort(cohortId);
		Context.getCohortService().addPatientToCohort(c, p);
	}
	
	/**
	 * Removes the {@link Patient} identified by <code>patientId</code> from the {@link Cohort}
	 * identified by <code>cohortId</code>
	 * 
	 * @param cohortId - Identifies the {@link Cohort} to remove from
	 * @param patientId - Identifies the {@link Patient} to remove
	 */
	public void removePatientFromCohort(Integer cohortId, Integer patientId) {
		Patient p = Context.getPatientService().getPatient(patientId);
		Cohort c = Context.getCohortService().getCohort(cohortId);
		Context.getCohortService().removePatientFromCohort(c, p);
	}
	
	/**
	 * Returns a Vector<ListItem> of all saved Cohorts
	 * 
	 * @return Vector<ListItem> - all saved Cohorts
	 */
	public Vector<ListItem> getCohorts() {
		Vector<ListItem> ret = new Vector<ListItem>();
		for (Cohort c : Context.getCohortService().getAllCohorts()) {
			ret.add(new ListItem(c.getCohortId(), c.getName(), c.getDescription()));
		}
		return ret;
	}
	
	/**
	 * Returns a Vector<ListItem> of all saved Cohorts containing the {@link Patient} identified by
	 * <code>patientId</code>
	 * 
	 * @param patientId - Identifies the {@link Patient} to lookup in each {@link Cohort}
	 * @return Vector<ListItem> - of all saved Cohorts containing the {@link Patient} identified by
	 *         <code>patientId</code>
	 */
	public Vector<ListItem> getCohortsContainingPatient(Integer patientId) {
		Vector<ListItem> ret = new Vector<ListItem>();
		for (Cohort c : Context.getCohortService().getCohortsContainingPatientId(patientId)) {
			ret.add(new ListItem(c.getCohortId(), c.getName(), c.getDescription()));
		}
		return ret;
	}
	
}

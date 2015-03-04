/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import org.openmrs.Cohort;
import org.openmrs.report.EvaluationContext;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class CohortFilter extends AbstractPatientFilter implements PatientFilter {
	
	private Cohort cohort;
	
	public CohortFilter() {
	}
	
	public CohortFilter(Cohort cohort) {
		this.cohort = cohort;
	}
	
	public String getName() {
		if (getCohort() != null) {
			return cohort.getName();
		} else {
			return super.getName();
		}
	}
	
	public String getDescription() {
		if (getCohort() != null) {
			return cohort.getDescription();
		} else {
			return super.getDescription();
		}
	}
	
	public Cohort filter(Cohort input, EvaluationContext context) {
		Cohort temp = new Cohort();
		if (getCohort() != null) {
			temp = getCohort();
		}
		return input == null ? temp : Cohort.intersect(input, temp);
	}
	
	public boolean isReadyToRun() {
		return cohort != null;
	}
	
	// getters and setters
	
	public Cohort getCohort() {
		return cohort;
	}
	
	public void setCohort(Cohort cohort) {
		this.cohort = cohort;
	}
	
}

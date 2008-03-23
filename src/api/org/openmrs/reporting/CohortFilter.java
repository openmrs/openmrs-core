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
package org.openmrs.reporting;

import org.openmrs.Cohort;

public class CohortFilter extends AbstractPatientFilter implements PatientFilter {

	private Cohort cohort;
	
	public CohortFilter() { }
	
	public CohortFilter(Cohort cohort) {
		this.cohort = cohort;
	}
	
	public String getName() {
		if (getCohort() != null)
			return getCohort().getName();
		else
			return super.getName();
	}
	
	public String getDescription() {
		if (getCohort() != null)
			return getCohort().getDescription();
		else
			return super.getDescription();
	}
	
	public PatientSet filter(PatientSet input) {
		PatientSet temp = new PatientSet();
		if (getCohort() != null)
			temp.copyPatientIds(getCohort().getMemberIds());
		return input == null ? temp : input.intersect(temp);
	}

	public PatientSet filterInverse(PatientSet input) {
		PatientSet temp = new PatientSet();
		if (getCohort() != null)
			temp.copyPatientIds(getCohort().getMemberIds());
		return input.subtract(temp);
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

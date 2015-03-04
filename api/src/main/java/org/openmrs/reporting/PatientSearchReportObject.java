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

import org.openmrs.util.OpenmrsConstants;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class PatientSearchReportObject extends AbstractReportObject {
	
	private PatientSearch patientSearch;
	
	public PatientSearchReportObject() {
		super.setType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);
		super.setSubType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);
	}
	
	public PatientSearchReportObject(String name, PatientSearch search) {
		this();
		setName(name);
		setPatientSearch(search);
	}
	
	public PatientSearch getPatientSearch() {
		return patientSearch;
	}
	
	public void setPatientSearch(PatientSearch patientSearch) {
		this.patientSearch = patientSearch;
	}
	
}

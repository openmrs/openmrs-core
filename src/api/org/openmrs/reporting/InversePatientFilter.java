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

public class InversePatientFilter extends AbstractPatientFilter implements PatientFilter {
	
	private PatientFilter baseFilter;
	
	public InversePatientFilter() {	}
	
	public InversePatientFilter(PatientFilter baseFilter) {
		this();
		this.baseFilter = baseFilter;
	}

	public PatientFilter getBaseFilter() {
		return baseFilter;
	}

	public void setBaseFilter(PatientFilter baseFilter) {
		this.baseFilter = baseFilter;
	}

	public PatientSet filter(PatientSet input) {
		return baseFilter.filterInverse(input);
	}

	public PatientSet filterInverse(PatientSet input) {
		return baseFilter.filter(input);
	}

	public boolean isReadyToRun() {
		// TODO Auto-generated method stub
		return baseFilter != null;
	}
	
	public String getDescription() {
		return "NOT " + (baseFilter == null ? "?" : baseFilter.getDescription());
	}

}

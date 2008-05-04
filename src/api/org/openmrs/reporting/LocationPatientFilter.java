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

import org.openmrs.Location;
import org.openmrs.api.PatientSetService.PatientLocationMethod;
import org.openmrs.api.context.Context;

public class LocationPatientFilter extends AbstractPatientFilter implements
		PatientFilter {
	
	private Location location;
	private PatientLocationMethod calculationMethod;
	
	public LocationPatientFilter() {
		calculationMethod = PatientLocationMethod.PATIENT_HEALTH_CENTER;
	}
	
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Patients who belong to ");
		sb.append(getLocation() == null ? "NULL" : getLocation().getName());
		sb.append(" (by method " + getCalculationMethod() + ")");
		return sb.toString();
	}
	
	public PatientSet filter(PatientSet input) {
		PatientSet ps = Context.getPatientSetService().getPatientsHavingLocation(getLocation(), getCalculationMethod());
		return input == null ? ps : input.intersect(ps);
	}

	public PatientSet filterInverse(PatientSet input) {
		PatientSet ps = Context.getPatientSetService().getPatientsHavingLocation(getLocation(), getCalculationMethod());
		return input.subtract(ps);
	}

	public boolean isReadyToRun() {
		return true;
	}
	
	// getters and setters

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public PatientLocationMethod getCalculationMethod() {
		return calculationMethod;
	}

	public void setCalculationMethod(PatientLocationMethod method) {
		this.calculationMethod = method;
	}
	
}

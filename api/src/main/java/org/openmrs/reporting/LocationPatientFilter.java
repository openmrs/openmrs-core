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
import org.openmrs.Location;
import org.openmrs.api.PatientSetService.PatientLocationMethod;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.report.EvaluationContext;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class LocationPatientFilter extends CachingPatientFilter {
	
	private Location location;
	
	private PatientLocationMethod calculationMethod;
	
	public LocationPatientFilter() {
		calculationMethod = PatientLocationMethod.PATIENT_HEALTH_CENTER;
	}
	
	@Override
	public String getCacheKey() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName()).append(".");
		sb.append(getCalculationMethod()).append(".");
		if (getLocation() != null)
			sb.append(getLocation().getLocationId());
		return sb.toString();
	}
	
	public String getDescription() {
		MessageSourceService msa = Context.getMessageSourceService();
		StringBuilder sb = new StringBuilder();
		sb.append(msa.getMessage("reporting.patientsWhoBelongTo")).append(" ");
		sb.append(getLocation() == null ? msa.getMessage("reporting.null") : getLocation().getName());
		sb.append(" (").append(msa.getMessage("reporting.byMethod")).append(" ").append(getCalculationMethod()).append(")");
		return sb.toString();
	}
	
	@Override
	public Cohort filterImpl(EvaluationContext context) {
		return Context.getPatientSetService().getPatientsHavingLocation(getLocation(), getCalculationMethod());
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

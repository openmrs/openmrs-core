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
		return input.intersect(ps);
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

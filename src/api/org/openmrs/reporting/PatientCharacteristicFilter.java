package org.openmrs.reporting;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.PatientService;
import java.util.*;

public class PatientCharacteristicFilter implements PatientFilter {

	Context context;
	String gender; // "M" or "F"
	Integer minAge;
	Integer maxAge;
	
	public PatientCharacteristicFilter() { }
	
	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Integer getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}

	public Integer getMinAge() {
		return minAge;
	}

	public void setMinAge(Integer minAge) {
		this.minAge = minAge;
	}
	
	public Context getContext() {
		return context;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}

	final static long ms_per_year = 1000l * 60 * 60 * 24 * 365;
	
	public PatientSet filter(PatientSet input) {
		PatientService patientService = context.getPatientService();
		PatientSet toReturn = new PatientSet();
		boolean checkGender = gender != null;
		boolean checkAge = minAge != null || maxAge != null;
		int minAgeInt = minAge == null ? -1 : minAge.intValue();
		int maxAgeInt = maxAge == null ? Integer.MAX_VALUE : maxAge.intValue();
		for (Iterator<Integer> i = input.getPatientIds().iterator(); i.hasNext(); ) {
			Patient patient = patientService.getPatient(i.next());
			if (checkGender && !gender.equals(patient.getGender())) {
				continue;
			}
			if (checkAge) {
				int age = (int) ((System.currentTimeMillis() - patient.getBirthdate().getTime()) / ms_per_year);
				if (age < minAgeInt || age > maxAgeInt) {
					continue;
				}
			}
			toReturn.add(patient);
		}
		return toReturn;
	}
	
}

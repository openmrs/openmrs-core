package org.openmrs.oldreporting;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.AbstractReportObject;

public class PatientCharacteristicFilter extends AbstractReportObject implements DataFilter<Patient> {

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
	
	public <P extends Patient> DataSet<Patient> filter(DataSet<P> input) {
		DataSet<Patient> toReturn = new SimpleDataSet<Patient>();
		boolean checkGender = gender != null;
		boolean checkAge = minAge != null || maxAge != null;
		int minAgeInt = minAge == null ? -1 : minAge.intValue();
		int maxAgeInt = maxAge == null ? Integer.MAX_VALUE : maxAge.intValue();
		for (P patient : input.getRowKeys()) {
			if (checkGender && !gender.equals(patient.getGender())) {
				continue;
			}
			if (checkAge) {
				int age = (int) ((System.currentTimeMillis() - patient.getBirthdate().getTime()) / ms_per_year);
				if (age < minAgeInt || age > maxAgeInt) {
					continue;
				}
			}
			toReturn.setRow(patient, input.getRow(patient));			
		}
		return toReturn;
	}
	
	public String getDescription() {
		StringBuffer ret = new StringBuffer();
		if (gender != null) {
			if ("m".equals(gender) || "M".equals(gender)) {
				ret.append("Male");
			} else {
				ret.append("Female");
			}
		}
		ret.append(" patients ");
		if (minAge != null) {
			if (maxAge != null) {
				ret.append(" between " + minAge + " and " + maxAge);
			} else {
				ret.append(" older than " + minAge);
			}
		} else {
			if (maxAge != null) {
				ret.append(" younger than " + maxAge);
			}			
		}
		return ret.toString();
	}
	
}

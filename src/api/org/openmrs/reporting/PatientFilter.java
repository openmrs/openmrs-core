package org.openmrs.reporting;

public interface PatientFilter {

	/**
	 * Filter a set of patient according to some data- or calculation-driven criteria
	 * @return a subset of the inputted PatientSet
	 */
	public PatientSet filter(PatientSet input);
	
}

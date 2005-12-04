package org.openmrs.reporting;

import java.util.Map;

/**
 * Used to split a PatientDataSet into subsets
 * @author djazayeri
 */
public interface PatientDataGrouper {
	
	/**
	 * Splits a PatientDataSet into subsets
	 * @param input	the PatientDataSet to split
	 * @return	A map from keys (of some sort) to PatientDataSets, which unioned together should add up to input.
	 */
	public Map<Object, PatientDataSet> groupPatientData(PatientDataSet input);
	
}

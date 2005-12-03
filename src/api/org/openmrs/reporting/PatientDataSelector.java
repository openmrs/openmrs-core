package org.openmrs.reporting;

import java.util.List;

/**
 * Pulls data about a specific PatientSet from the database, and possibly does calculations on it. 
 * @author djazayeri
 */
public interface PatientDataSelector {

	/**
	 * @return the names of the data items that this selector will return in its PatientDataSet
	 */
	public List<String> getDataItemNames();

	/**
	 * @return per-patient data and/or calculations. 
	 */
	public PatientDataSet getData(PatientSet ps);
	
}

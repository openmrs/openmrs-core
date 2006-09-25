package org.openmrs.reporting;

import java.util.Locale;

public interface PatientDataSetFormatter {

	public Object format(PatientDataSet input, Locale locale);
	
}

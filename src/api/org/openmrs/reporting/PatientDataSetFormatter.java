package org.openmrs.reporting;

/**
 * Formats a PatientDataSet for output to a human
 * @author djazayeri
 */
public interface PatientDataSetFormatter extends OutputFormatter {

	/**
	 * Formats a PatientDataSet input into some human-readable output format (e.g. HTML, CSV, PDF, plaintext)
	 */
	public Object format(PatientDataSet input);
	
}

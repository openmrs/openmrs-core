package org.openmrs.reporting;


public interface PatientFilter extends ReportObject {

	/**
	 * Determine all patients in _input_ who also match some criteria
	 * @param context
	 * @param input
	 * @return
	 */
	public PatientSet filter(PatientSet input);

	/**
	 * Determine all patients in _input_ who do *not* match some criteria
	 * @param context
	 * @param input
	 * @return
	 */
	public PatientSet filterInverse(PatientSet input);

	/**
	 * @return Whether or not this filter has had enough parameters set to be run properly
	 */
	public boolean isReadyToRun();
}

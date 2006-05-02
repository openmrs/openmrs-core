package org.openmrs.reporting;

import org.openmrs.api.context.Context;

public interface PatientFilter extends ReportObject {

	/**
	 * Determine all patients in _input_ who also match some criteria
	 * @param context
	 * @param input
	 * @return
	 */
	public PatientSet filter(Context context, PatientSet input);

	/**
	 * Determine all patients in _input_ who do *not* match some criteria
	 * @param context
	 * @param input
	 * @return
	 */
	public PatientSet filterInverse(Context context, PatientSet input);

}

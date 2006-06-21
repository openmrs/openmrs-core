package org.openmrs.reporting;

import java.util.Map;

import org.openmrs.api.context.Context;

public interface PatientClassifier {

	/**
	 * Partition all patients in _input_ into subsets. (Unioned together these subsets should make up _input_.) 
	 * @param context
	 * @param input
	 * @return A map of label -> subset 
	 */
	public Map<String, PatientSet> partition(Context context, PatientSet input);

	public String getDescription();
	
}

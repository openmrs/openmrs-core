package org.openmrs.reporting;

import java.util.Map;

public interface PatientDataSetAggregator {

	public Map<Object, Object> aggregatePatientDataSets(Map<Object, PatientDataSet> input);
	
}

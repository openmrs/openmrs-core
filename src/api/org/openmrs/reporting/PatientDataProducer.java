package org.openmrs.reporting;

import java.util.Map;

import org.openmrs.api.context.Context;

public interface PatientDataProducer {

	public Map<Integer, Object> produceData(Context context, PatientSet patients);
	
}

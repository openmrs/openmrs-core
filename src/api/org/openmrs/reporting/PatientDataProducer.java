package org.openmrs.reporting;

import java.util.Collection;
import java.util.Map;

public interface PatientDataProducer extends ReportObject {
	public Map<Integer, Object> produceData(Collection<Integer> patientIds);
}

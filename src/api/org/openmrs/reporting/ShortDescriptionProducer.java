package org.openmrs.reporting;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.api.context.Context;

public class ShortDescriptionProducer extends AbstractPatientDataProducer implements PatientDataProducer {

	public Map<Integer, Object> produceData(Context context, PatientSet patients) {
		Map<Integer, String> temp = context.getPatientSetService().getShortPatientDescriptions(patients);
		Map<Integer, Object> ret = new HashMap<Integer, Object>();
		for (Map.Entry<Integer, String> e : temp.entrySet()) {
			ret.put(e.getKey(), e.getValue());
		}
		return ret;
	}

}

package org.openmrs.reporting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;

public class ObsListProducer extends AbstractReportObject implements PatientDataProducer {

	Concept concept;
	
	public ObsListProducer() { }
	
	public ObsListProducer(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * @param concept The concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	public Map<Integer, Object> produceData(Context context, PatientSet patients) {
		PatientSetService patientSetService = context.getPatientSetService();
		Map<Integer, List<Obs>> temp = patientSetService.getObservations(patients, concept);
		Map<Integer, Object> ret = new HashMap<Integer, Object>();
		for (Map.Entry<Integer, List<Obs>> e : temp.entrySet()) {
			ret.put(e.getKey(), e.getValue());
		}
		return ret;
	}

}

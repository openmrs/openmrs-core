package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.api.context.Context;

public class PatientAnalysis extends AbstractReportObject {

	private LinkedHashMap<String, PatientFilter> patientFilters;
	private PatientClassifier patientClassifier;
	private List<PatientDataProducer> patientDataProducers; 
	
	public PatientAnalysis() {
		patientFilters = new LinkedHashMap<String, PatientFilter>();
		patientDataProducers = new ArrayList<PatientDataProducer>();
	}
	
	public PatientClassifier getPatientClassifier() {
		return patientClassifier;
	}

	public void setPatientClassifier(PatientClassifier patientClassifier) {
		this.patientClassifier = patientClassifier;
	}

	/**
	 * @return Returns the patientFilters.
	 */
	public Map<String, PatientFilter> getPatientFilters() {
		return patientFilters;
	}

	/**
	 * @param patientFilters The patientFilters to set.
	 */
	public void setPatientFilters(Map<String, PatientFilter> patientFilters) {
		this.patientFilters = new LinkedHashMap<String, PatientFilter>(patientFilters);
	}

	/**
	 * @return Returns the patientDataProducers.
	 */
	public List<PatientDataProducer> getPatientDataProducers() {
		return patientDataProducers;
	}

	/**
	 * @param patientDataProducers The patientDataProducers to set.
	 */
	public void setPatientDataProducers(
			List<PatientDataProducer> patientDataProducers) {
		this.patientDataProducers = patientDataProducers;
	}

	public String addFilter(String name, PatientFilter pf) {
		if (patientFilters.values().contains(pf)) {
			return null;
		}
		if (name == null) {
			do {
				name = "" + System.currentTimeMillis() + "" + Math.random();
			} while (patientFilters.containsKey(name));
		}
		patientFilters.put(name, pf);
		return name;
	}
	
	public PatientFilter removeFilter(String name) {
		return patientFilters.remove(name);
	}
	
	public boolean removeFilter(PatientFilter pf) {
		return patientFilters.values().remove(pf);
	}
		
	/* (non-Javadoc)
	 * @see java.util.List#add(E)
	 */
	public boolean addProducer(PatientDataProducer o) {
		return patientDataProducers.add(o);
	}

	/* (non-Javadoc)
	 * @see java.util.List#remove(int)
	 */
	public PatientDataProducer removeProducer(int index) {
		return patientDataProducers.remove(index);
	}

	/* (non-Javadoc)
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean removeProducer(PatientDataProducer o) {
		return patientDataProducers.remove(o);
	}

	public PatientSet runFilters(PatientSet input) {
		if (input == null)
			input = Context.getPatientSetService().getAllPatients();
		PatientSet ret = input;
		for (PatientFilter pf : patientFilters.values()) {
			ret = pf.filter(ret);
		}
		return ret;
	}
	
	public Map<String, PatientSet> runFiltersAndClassifier(PatientSet input) {
		input = runFilters(input);
		if (patientClassifier == null) {
			Map<String, PatientSet> ret = new HashMap<String, PatientSet>();
			ret.put("", input);
			return ret;
		} else {
			return patientClassifier.partition(input);
		}
	}
	
}

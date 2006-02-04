package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.api.context.Context;

public class PatientAnalysis extends AbstractReportObject {

	private List<PatientFilter> patientFilters;
	private List<PatientDataProducer> patientDataProducers; 
	
	public PatientAnalysis() {
		patientFilters = new ArrayList<PatientFilter>();
		patientDataProducers = new ArrayList<PatientDataProducer>();
	}
		
	/**
	 * @return Returns the patientFilters.
	 */
	public List<PatientFilter> getPatientFilters() {
		return patientFilters;
	}

	/**
	 * @param patientFilters The patientFilters to set.
	 */
	public void setPatientFilters(List<PatientFilter> patientFilters) {
		this.patientFilters = patientFilters;
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

	public void addFilter(PatientFilter pf) {
		if (!patientFilters.contains(pf)) {
			patientFilters.add(pf);
		}
	}
	
	public PatientFilter removeFilter(int index) {
		return patientFilters.remove(index);
	}
	
	public boolean removeFilter(PatientFilter pf) {
		return patientFilters.remove(pf);
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

	public PatientSet runFilters(Context context, PatientSet input) {
		PatientSet ret = input;
		for (PatientFilter pf : patientFilters) {
			ret = pf.filter(context, ret);
		}
		return ret;
	}
	
}

package org.openmrs.reporting;

public class InversePatientFilter extends AbstractPatientFilter implements PatientFilter {
	
	private PatientFilter baseFilter;
	
	public InversePatientFilter() {	}
	
	public InversePatientFilter(PatientFilter baseFilter) {
		this();
		this.baseFilter = baseFilter;
	}

	public PatientFilter getBaseFilter() {
		return baseFilter;
	}

	public void setBaseFilter(PatientFilter baseFilter) {
		this.baseFilter = baseFilter;
	}

	public PatientSet filter(PatientSet input) {
		return baseFilter.filterInverse(input);
	}

	public PatientSet filterInverse(PatientSet input) {
		return baseFilter.filter(input);
	}

	public boolean isReadyToRun() {
		// TODO Auto-generated method stub
		return baseFilter != null;
	}
	
	public String getDescription() {
		return "NOT " + (baseFilter == null ? "?" : baseFilter.getDescription());
	}

}

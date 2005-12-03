package org.openmrs.reporting;

/**
 * A PatientFilter that can tell you what data it needs, so that a report generator can potentially
 * combine selects for multiple filters to improve efficiency.
 * (This is just a placeholder. More methods are probably needed here.)      
 */
public interface OptimizablePatientFilter extends PatientFilter {

	public String getDataNeeded();
	
	public void provideData(Object data);
	
	public PatientSet filter(PatientSet input);

}

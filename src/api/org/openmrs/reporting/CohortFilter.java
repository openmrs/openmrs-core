package org.openmrs.reporting;

import org.openmrs.Cohort;

public class CohortFilter extends AbstractPatientFilter implements PatientFilter {

	private Cohort cohort;
	
	public CohortFilter() { }
	
	public CohortFilter(Cohort cohort) {
		this.cohort = cohort;
	}
	
	public String getName() {
		if (getCohort() != null)
			return getCohort().getName();
		else
			return super.getName();
	}
	
	public String getDescription() {
		if (getCohort() != null)
			return getCohort().getDescription();
		else
			return super.getDescription();
	}
	
	public PatientSet filter(PatientSet input) {
		PatientSet temp = new PatientSet();
		if (getCohort() != null)
			temp.copyPatientIds(getCohort().getMemberIds());
		return input == null ? temp : input.intersect(temp);
	}

	public PatientSet filterInverse(PatientSet input) {
		PatientSet temp = new PatientSet();
		if (getCohort() != null)
			temp.copyPatientIds(getCohort().getMemberIds());
		return input.subtract(temp);
	}

	public boolean isReadyToRun() {
		return cohort != null;
	}
	
	// getters and setters

	public Cohort getCohort() {
		return cohort;
	}

	public void setCohort(Cohort cohort) {
		this.cohort = cohort;
	}

}

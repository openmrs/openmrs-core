package org.openmrs.api;

import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.db.CohortDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CohortService {
	
	public void setCohortDAO(CohortDAO dao);
	
	public void createCohort(Cohort cohort);
	
	public void updateCohort(Cohort cohort);
	
	public void voidCohort(Cohort cohort, String reason);
	
	@Transactional(readOnly=true)
	public Cohort getCohort(Integer id);
	
	@Transactional(readOnly=true)
	public List<Cohort> getCohorts();
	
	@Transactional(readOnly=true)
	public List<Cohort> getCohortsContainingPatient(Patient patient);
	
	@Transactional(readOnly=true)
	public List<Cohort> getCohortsContainingPatientId(Integer patientId);
	
	@Transactional
	public void addPatientToCohort(Cohort cohort, Patient patient);
	
	@Transactional
	public void removePatientFromCohort(Cohort cohort, Patient patient);

}

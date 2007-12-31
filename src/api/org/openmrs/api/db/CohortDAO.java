package org.openmrs.api.db;

import java.util.List;
import org.openmrs.Cohort;

public interface CohortDAO {
	
	public void createCohort(Cohort cohort) throws DAOException;
	
	public Cohort getCohort(Integer id) throws DAOException;
	
	public List<Cohort> getCohorts() throws DAOException;
	
	public List<Cohort> getCohortsContainingPatientId(Integer patientId) throws DAOException;
	
	public void updateCohort(Cohort cohort) throws DAOException;

}

package org.openmrs.api.db;

import org.openmrs.Cohort;

public interface CohortDAO {
	
	public void createCohort(Cohort cohort) throws DAOException;
	
	public Cohort getCohort(Integer id) throws DAOException;

}

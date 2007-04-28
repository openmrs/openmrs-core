package org.openmrs.api.db;

import java.util.List;

import org.openmrs.Cohort;

public interface CohortDAO {
	
	public void createCohort(Cohort cohort) throws DAOException;
	
	public Cohort getCohort(Integer id) throws DAOException;
	
	public List<Cohort> getCohorts() throws DAOException;

}

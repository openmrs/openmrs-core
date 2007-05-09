package org.openmrs.api.impl;

import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.CohortDAO;

public class CohortServiceImpl implements CohortService {

	private CohortDAO dao;
	
	public CohortServiceImpl() { }
	
	private CohortDAO getCohortDAO() {
		return dao;
	}
	
	public void setCohortDAO(CohortDAO dao) {
		this.dao = dao;
	}
	
	public void createCohort(Cohort cohort) {
		if (cohort.getCreator() == null)
			cohort.setCreator(Context.getAuthenticatedUser());
		if (cohort.getDateCreated() == null)
			cohort.setDateCreated(new java.util.Date());
		if (cohort.getName() == null)
			throw new IllegalArgumentException("Missing Name");
		getCohortDAO().createCohort(cohort);
	}

	public Cohort getCohort(Integer id) {
		return getCohortDAO().getCohort(id); 
	}
	
	public List<Cohort> getCohorts() {
		return getCohortDAO().getCohorts();
	}

}

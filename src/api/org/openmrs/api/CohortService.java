package org.openmrs.api;

import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.api.db.CohortDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CohortService {
	
	public void setCohortDAO(CohortDAO dao);
	
	public void createCohort(Cohort cohort);
	
	public Cohort getCohort(Integer id);
	
	public List<Cohort> getCohorts();

}

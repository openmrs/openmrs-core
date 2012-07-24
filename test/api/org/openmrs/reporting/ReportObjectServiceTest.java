package org.openmrs.reporting;


import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

public class ReportObjectServiceTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link ReportObjectService#saveSearchHistory(CohortSearchHistory)}
	 * 
	 */
	@Test
	@Verifies(value = "should save history successfully", method = "saveSearchHistory(CohortSearchHistory)")
	public void saveSearchHistory_shouldSaveHistorySuccessfully() throws Exception {
		// make a patient search object
		PatientSearch search = new PatientSearch();
		search.setFilterClass(ObsPatientFilter.class);
		List<SearchArgument> args = new ArrayList<SearchArgument>();
		args.add(new SearchArgument("timeModifier", "ANY", PatientSetService.TimeModifier.class));
		args.add(new SearchArgument("question", Context.getConceptService().getConceptByName("CD4 COUNT").getConceptId()
		        .toString(), Concept.class));
		args.add(new SearchArgument("withinLastDays", "${howManyDays}", Integer.class));
		search.setArguments(args);
		
		// save the object to the database
		CohortSearchHistory history = new CohortSearchHistory();
		history.setName("Some name");
		history.setDescription("a description");
		history.addSearchItem(search);
		Context.getReportObjectService().saveSearchHistory(history);
	}
}
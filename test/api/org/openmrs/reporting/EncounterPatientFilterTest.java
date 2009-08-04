package org.openmrs.reporting;


import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsUtil;

public class EncounterPatientFilterTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link EncounterPatientFilter#filterImpl(EvaluationContext)}
	 * 
	 */
	@Test
	@Verifies(value = "should get patients given multiple encounter types", method = "filterImpl(EvaluationContext)")
	public void filterImpl_shouldGetPatientsGivenMultipleEncounterTypes() throws Exception {
		executeDataSet("org/openmrs/api/include/PatientSetServiceTest-extraData.xml");
		PatientSearch ps = new PatientSearch();
		ps.setFilterClass(EncounterPatientFilter.class);
		ps.addArgument("encounterTypeList", "1", EncounterType.class);
		Cohort withOneType = Context.getCohortService().evaluate(ps, new EvaluationContext());
    	Assert.assertEquals(1, withOneType.size());
    	ps.addArgument("encounterTypeList", "6", EncounterType.class);
		Cohort withTwoTypes = Context.getCohortService().evaluate(ps, new EvaluationContext());
    	Assert.assertEquals(2, withTwoTypes.size());
	}
}
package org.openmrs.web.dwr;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class DWRConceptServiceTest extends BaseContextSensitiveTest {
	
	@Before
	public void before() throws Exception {
		executeDataSet("org/openmrs/web/dwr/include/DWRConceptServiceTest-coded-concept-with-no-answers.xml");
	}
	
	/**
	 * @see DWRConceptService#findConceptAnswers(String,Integer,boolean,boolean)
	 * @verifies not fail if the specified concept has no answers (regression test for TRUNK-2807)
	 */
	@Test
	public void findConceptAnswers_shouldNotFailIfTheSpecifiedConceptHasNoAnswersRegressionTestForTRUNK2807()
	        throws Exception {
		new DWRConceptService().findConceptAnswers("", 1000, false, true);
		// if we got here, we've passed, because we didn't get a NullPointerException
	}
}

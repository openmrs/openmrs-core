/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.reporting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.report.EvaluationContext;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 *
 */
public class CachingPatientFilterTest extends BaseContextSensitiveTest {

	@Test
	public void shouldCachingPatientFilter() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/reporting/include/PatientFilterTest.xml");
		authenticate();
		
		EvaluationContext ec = new EvaluationContext();
		
		CachingPatientFilter maleFilter = new PatientCharacteristicFilter("M", null, null);
		CachingPatientFilter femaleFilter = new PatientCharacteristicFilter("F", null, null);
		String maleKey = maleFilter.getCacheKey();
		String femaleKey = femaleFilter.getCacheKey();
		
		assertNull("Cache should not have male filter yet", ec.getFromCache(maleKey));
		Cohort males = maleFilter.filter(null, ec);
		assertNotNull("Cache should have male filter now", ec.getFromCache(maleKey));
		assertNull("Cache should not have female filter", ec.getFromCache(femaleKey));
		Cohort malesAgain = maleFilter.filter(null, ec);
		assertEquals("Uncached and cached runs should be equals", males.size(), malesAgain.size());
		ec.setBaseCohort(males);
		assertEquals("Cache should have been automatically cleared", 0, ec.getCache().size());
	}

}

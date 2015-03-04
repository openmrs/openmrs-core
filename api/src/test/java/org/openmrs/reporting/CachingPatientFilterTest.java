/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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

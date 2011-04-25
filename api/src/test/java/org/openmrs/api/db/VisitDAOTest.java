/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License")throws DAOException; you may not use this file except in
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
package org.openmrs.api.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.VisitService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Contains the tests for {@link VisitDAO} methods that don't have equivalents at the
 * {@link VisitService} layer
 */
public class VisitDAOTest extends BaseContextSensitiveTest {
	
	protected static final String VISITS_WITH_DATES_XML = "org/openmrs/api/include/VisitServiceTest-otherVisits.xml";
	
	private VisitDAO dao = null;
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		
		if (dao == null)
			// fetch the dao from the spring application context
			dao = (VisitDAO) applicationContext.getBean("visitDAO");
	}
	
	/**
	 * @see {@link VisitDAO#getVisits(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Date, java.util.Date, java.util.Date, java.util.Date, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should return all unvoided visits if includeEnded is set to true", method = "getVisits(Collection<VisitType>,Collection<Patient>,Collection<Location>,Collection<Concept>,Date,Date,Date,Date,boolean,boolean)")
	public void getVisits_shouldReturnAllUnvoidedVisitsIfIncludeEndedIsSetToTrue() throws Exception {
		executeDataSet(VISITS_WITH_DATES_XML);
		Assert.assertEquals(13, dao.getVisits(null, null, null, null, null, null, null, null, true, false).size());
	}
	
	/**
	 * @see {@link VisitDAO#getVisits(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Date, java.util.Date, java.util.Date, java.util.Date, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should return only active visits if includeEnded is set to false", method = "getVisits(Collection<VisitType>,Collection<Patient>,Collection<Location>,Collection<Concept>,Date,Date,Date,Date,boolean,boolean)")
	public void getVisits_shouldReturnOnlyActiveVisitsIfIncludeEndedIsSetToFalse() throws Exception {
		executeDataSet(VISITS_WITH_DATES_XML);
		Assert.assertEquals(6, dao.getVisits(null, null, null, null, null, null, null, null, false, false).size());
	}
}

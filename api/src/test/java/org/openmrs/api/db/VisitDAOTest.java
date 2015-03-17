/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.ArrayList;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Contains the tests for {@link VisitDAO} methods that don't have equivalents at the
 * {@link VisitService} layer
 */
public class VisitDAOTest extends BaseContextSensitiveTest {
	
	protected static final String VISITS_WITH_DATES_XML = "org/openmrs/api/include/VisitServiceTest-otherVisits.xml";
	
	protected static final String VISITS_INCLUDE_VISITS_TO_AUTO_CLOSE_XML = "org/openmrs/api/include/VisitServiceTest-includeVisitsAndTypeToAutoClose.xml";
	
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
		Assert.assertEquals(13, dao.getVisits(null, null, null, null, null, null, null, null, null, true, false).size());
	}
	
	/**
	 * @see {@link VisitDAO#getVisits(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Date, java.util.Date, java.util.Date, java.util.Date, boolean, boolean)}
	 */
	@Test
	@Verifies(value = "should return only active visits if includeEnded is set to false", method = "getVisits(Collection<VisitType>,Collection<Patient>,Collection<Location>,Collection<Concept>,Date,Date,Date,Date,boolean,boolean)")
	public void getVisits_shouldReturnOnlyActiveVisitsIfIncludeEndedIsSetToFalse() throws Exception {
		executeDataSet(VISITS_WITH_DATES_XML);
		Assert.assertEquals(6, dao.getVisits(null, null, null, null, null, null, null, null, null, false, false).size());
	}
	
	/**
	 * @see {@link VisitDAO#getNextVisit(Visit,Collection<VisitType>,Date)}
	 */
	@Test
	@Verifies(value = "should return the next unvoided active visit matching the specified types and startDate", method = "getNextVisit(Visit,Collection<VisitType>,Date)")
	public void getNextVisit_shouldReturnTheNextUnvoidedActiveVisitMatchingTheSpecifiedTypesAndStartDate() throws Exception {
		executeDataSet(VISITS_INCLUDE_VISITS_TO_AUTO_CLOSE_XML);
		ArrayList<VisitType> visitTypes = new ArrayList<VisitType>();
		visitTypes.add(dao.getVisitType(4));
		Calendar cal = Calendar.getInstance();
		cal.set(2005, 0, 4, 23, 59, 59);
		cal.set(Calendar.MILLISECOND, 999);
		Assert.assertEquals(105, dao.getNextVisit(dao.getVisit(1), visitTypes, cal.getTime()).getVisitId().intValue());
	}
}

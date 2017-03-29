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
	public void runBeforeEachTest() {
		
		if (dao == null)
			// fetch the dao from the spring application context
			dao = (VisitDAO) applicationContext.getBean("visitDAO");
	}
	
	/**
	 * @see VisitDAO#getVisits(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Date, java.util.Date, java.util.Date, java.util.Date, boolean, boolean)
	 */
	@Test
	public void getVisits_shouldReturnAllUnvoidedVisitsIfIncludeEndedIsSetToTrue() {
		executeDataSet(VISITS_WITH_DATES_XML);
		Assert.assertEquals(13, dao.getVisits(null, null, null, null, null, null, null, null, null, true, false).size());
	}
	
	/**
	 * @see VisitDAO#getVisits(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Date, java.util.Date, java.util.Date, java.util.Date, boolean, boolean)
	 */
	@Test
	public void getVisits_shouldReturnOnlyActiveVisitsIfIncludeEndedIsSetToFalse() {
		executeDataSet(VISITS_WITH_DATES_XML);
		Assert.assertEquals(6, dao.getVisits(null, null, null, null, null, null, null, null, null, false, false).size());
	}
	
	/**
	 * @see VisitDAO#getNextVisit(Visit,Collection<VisitType>,Date)
	 */
	@Test
	public void getNextVisit_shouldReturnTheNextUnvoidedActiveVisitMatchingTheSpecifiedTypesAndStartDate() {
		executeDataSet(VISITS_INCLUDE_VISITS_TO_AUTO_CLOSE_XML);
		ArrayList<VisitType> visitTypes = new ArrayList<>();
		visitTypes.add(dao.getVisitType(4));
		Calendar cal = Calendar.getInstance();
		cal.set(2005, 0, 4, 23, 59, 59);
		cal.set(Calendar.MILLISECOND, 999);
		Assert.assertEquals(105, dao.getNextVisit(dao.getVisit(1), visitTypes, cal.getTime()).getVisitId().intValue());
	}
}

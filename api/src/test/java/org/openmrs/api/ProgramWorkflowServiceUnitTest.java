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
package org.openmrs.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.Program;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.api.impl.ProgramWorkflowServiceImpl;
import org.openmrs.test.Verifies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This class unit tests methods in the ProgramWorkflowService class.
 * Unlike ProgramWorkflowServiceTest, this class does not extend
 * BaseContextSensitiveTest so as not to auto-wire the dependencies
 * of PatientService, hence implementing true unit (and not integration) tests
 */
public class ProgramWorkflowServiceUnitTest {
	
	private ProgramWorkflowService pws;
	
	@Before
	public void setup() {
		pws = new ProgramWorkflowServiceImpl();
	}
	
	@Test
	@Verifies(value = "should call the DAO method getProgramsByName", method = "getProgramByName")
	public void getProgramByName_shouldCallDaoGetProgramsByName() {
		ProgramWorkflowDAO mockDao = Mockito.mock(ProgramWorkflowDAO.class);
		pws.setProgramWorkflowDAO(mockDao);
		pws.getProgramByName("A name");
		Mockito.verify(mockDao).getProgramsByName("A name", false);
		Mockito.verify(mockDao).getProgramsByName("A name", true);
	}
	
	@Test
	@Verifies(value = "should return null when DAO returns an empty list", method = "getProgramByName")
	public void getProgramByName_shouldReturnNullWhenThereIsNoProgramForGivenName() {
		ProgramWorkflowDAO mockDao = Mockito.mock(ProgramWorkflowDAO.class);
		List<Program> noProgramWithGivenName = new ArrayList<Program>();
		Mockito.stub(mockDao.getProgramsByName("A name", false)).toReturn(noProgramWithGivenName);
		Mockito.stub(mockDao.getProgramsByName("A name", true)).toReturn(noProgramWithGivenName);
		pws.setProgramWorkflowDAO(mockDao);
		Assert.assertNull(pws.getProgramByName("A name"));
	}
	
	@Test(expected = org.openmrs.api.ProgramNameDuplicatedException.class)
	@Verifies(value = "should fail when two programs found with same name", method = "getProgramByName()")
	public void getProgramByName_shouldFailWhenTwoProgramsFoundWithSameName() {
		ProgramWorkflowDAO mockDao = Mockito.mock(ProgramWorkflowDAO.class);
		List<Program> programsWithGivenName = new ArrayList<Program>();
		Program program1 = new Program("A name");
		Program program2 = new Program("A name");
		programsWithGivenName.add(program1);
		programsWithGivenName.add(program2);
		Mockito.stub(mockDao.getProgramsByName("A name", false)).toReturn(programsWithGivenName);
		Mockito.stub(mockDao.getProgramsByName("A name", true)).toReturn(programsWithGivenName);
		pws.setProgramWorkflowDAO(mockDao);
		pws.getProgramByName("A name");
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.api.impl.ProgramWorkflowServiceImpl;

import static org.junit.Assert.assertTrue;

/**
 * This class unit tests methods in the ProgramWorkflowService class.
 * Unlike ProgramWorkflowServiceTest, this class does not extend
 * BaseContextSensitiveTest so as not to auto-wire the dependencies
 * of PatientService, hence implementing true unit (and not integration) tests
 */
public class ProgramWorkflowServiceUnitTest {
	
	private ProgramWorkflowService pws;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		pws = new ProgramWorkflowServiceImpl();
	}
	
	@Test
	public void getProgramByName_shouldCallDaoGetProgramsByName() {
		ProgramWorkflowDAO mockDao = Mockito.mock(ProgramWorkflowDAO.class);
		pws.setProgramWorkflowDAO(mockDao);
		pws.getProgramByName("A name");
		Mockito.verify(mockDao).getProgramsByName("A name", false);
		Mockito.verify(mockDao).getProgramsByName("A name", true);
	}
	
	@Test
	public void getProgramByName_shouldReturnNullWhenThereIsNoProgramForGivenName() {
		ProgramWorkflowDAO mockDao = Mockito.mock(ProgramWorkflowDAO.class);
		List<Program> noProgramWithGivenName = new ArrayList<Program>();
		Mockito.stub(mockDao.getProgramsByName("A name", false)).toReturn(noProgramWithGivenName);
		Mockito.stub(mockDao.getProgramsByName("A name", true)).toReturn(noProgramWithGivenName);
		pws.setProgramWorkflowDAO(mockDao);
		Assert.assertNull(pws.getProgramByName("A name"));
	}
	
	@Test(expected = org.openmrs.api.ProgramNameDuplicatedException.class)
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

	@Test
	public void saveProgram_shouldFailForInvalidProgram() {
		Program program = new Program();
		expectedException.expect(APIException.class);
		pws.saveProgram(program);
	}

	@Test
	public void saveProgram_shouldFailForWorkflowConceptMissing() {
		Program program = new Program();
		program.setConcept(new Concept());
		ProgramWorkflow programWorkflow = new ProgramWorkflow();
		program.addWorkflow(programWorkflow);
		expectedException.expect(APIException.class);
		pws.saveProgram(program);
	}

	@Test
	public void saveProgram_shouldSaveIfWorkflowConceptNull() {
		ProgramWorkflowDAO mockDao = Mockito.mock(ProgramWorkflowDAO.class);
		Program program = new Program();
		program.setConcept(new Concept());
		ProgramWorkflow programWorkflow = new ProgramWorkflow();
		programWorkflow.setConcept(new Concept());
		program.addWorkflow(programWorkflow);
		Mockito.when(mockDao.saveProgram(Mockito.any(Program.class))).thenReturn(program);

		pws.setProgramWorkflowDAO(mockDao);
		Program savedProgram = pws.saveProgram(program);
		assertTrue(savedProgram.equals(program));
	}

	@Test
	public void saveProgram_shouldFailIfWorkflowStateConceptIsNull() {
		ProgramWorkflowDAO mockDao = Mockito.mock(ProgramWorkflowDAO.class);
		Program program = new Program();
		program.setConcept(new Concept());
		ProgramWorkflow programWorkflow = new ProgramWorkflow();
		programWorkflow.setConcept(new Concept());

		ProgramWorkflowState programWorkflowState = new ProgramWorkflowState();
		programWorkflow.addState(programWorkflowState);
		program.addWorkflow(programWorkflow);
		Mockito.when(mockDao.saveProgram(Mockito.any(Program.class))).thenReturn(program);

		pws.setProgramWorkflowDAO(mockDao);
		expectedException.expect(APIException.class);
		pws.saveProgram(program);
	}

	@Test
	public void saveProgram_shouldSaveIfWorkflowInStateNull() {
		ProgramWorkflowDAO mockDao = Mockito.mock(ProgramWorkflowDAO.class);
		Program program = new Program();
		program.setConcept(new Concept());
		ProgramWorkflow programWorkflow = new ProgramWorkflow();
		programWorkflow.setConcept(new Concept());

		ProgramWorkflowState programWorkflowState = new ProgramWorkflowState();
		programWorkflowState.setConcept(new Concept());
		programWorkflowState.setInitial(true);
		programWorkflowState.setTerminal(true);
		programWorkflow.addState(programWorkflowState);
		program.addWorkflow(programWorkflow);
		Mockito.when(mockDao.saveProgram(Mockito.any(Program.class))).thenReturn(program);

		pws.setProgramWorkflowDAO(mockDao);
		Program savedProgram = pws.saveProgram(program);
		assertTrue(savedProgram.equals(program));
	}
}

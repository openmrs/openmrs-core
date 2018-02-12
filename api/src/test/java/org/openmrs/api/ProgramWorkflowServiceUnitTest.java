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
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.api.impl.ProgramWorkflowServiceImpl;

/**
 * This class unit tests methods in the ProgramWorkflowService class.
 * Unlike ProgramWorkflowServiceTest, this class does not extend
 * BaseContextSensitiveTest so as not to auto-wire the dependencies
 * of PatientService, hence implementing true unit (and not integration) tests
 */
public class ProgramWorkflowServiceUnitTest {
	
	private ProgramWorkflowService pws;
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
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
		List<Program> noProgramWithGivenName = new ArrayList<>();
		Mockito.stub(mockDao.getProgramsByName("A name", false)).toReturn(noProgramWithGivenName);
		Mockito.stub(mockDao.getProgramsByName("A name", true)).toReturn(noProgramWithGivenName);
		pws.setProgramWorkflowDAO(mockDao);
		Assert.assertNull(pws.getProgramByName("A name"));
	}
	
	@Test(expected = org.openmrs.api.ProgramNameDuplicatedException.class)
	public void getProgramByName_shouldFailWhenTwoProgramsFoundWithSameName() {
		ProgramWorkflowDAO mockDao = Mockito.mock(ProgramWorkflowDAO.class);
		List<Program> programsWithGivenName = new ArrayList<>();
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
	public void saveProgram_shouldFailIfProgramConceptIsNull() {
		
		exception.expect(APIException.class);
		exception.expectMessage("Program concept is required");
		
		Program program1 = new Program(1);
		
		pws.saveProgram(program1);
	}
	
	@Test
	public void saveProgram_shouldFailIfProgramWorkFlowConceptIsNull() {
		
		exception.expect(APIException.class);
		exception.expectMessage("ProgramWorkflow concept is required");
		
		Program program = new Program();
		program.setName("TEST PROGRAM");
		program.setDescription("TEST PROGRAM DESCRIPTION");
		program.setConcept(new Concept(1));
		program.addWorkflow(new ProgramWorkflow());
		
		pws.saveProgram(program);
	}
	
	@Test
	public void saveProgram_shouldFailIfProgramWorkFlowStateConceptIsNull() {
		
		exception.expect(APIException.class);
		exception.expectMessage("ProgramWorkflowState concept, initial, terminal are required");
		
		Program program = new Program();
		program.setName("TEST PROGRAM");
		program.setDescription("TEST PROGRAM DESCRIPTION");
		program.setConcept(new Concept(1));
		
		ProgramWorkflow workflow = new ProgramWorkflow();
		workflow.setConcept(new Concept(2));
		
		ProgramWorkflowState state1 = new ProgramWorkflowState();
		state1.setInitial(true);
		state1.setTerminal(false);
		
		workflow.addState(state1);
		program.addWorkflow(workflow);
		
		pws.saveProgram(program);
	}
	
	@Test
	public void saveProgram_shouldFailIfProgramWorkFlowStateInitialIsNull() {
		
		exception.expect(APIException.class);
		exception.expectMessage("ProgramWorkflowState concept, initial, terminal are required");
		
		Program program = new Program();
		program.setName("TEST PROGRAM");
		program.setDescription("TEST PROGRAM DESCRIPTION");
		program.setConcept(new Concept(1));
		
		ProgramWorkflow workflow = new ProgramWorkflow();
		workflow.setConcept(new Concept(2));
		
		ProgramWorkflowState state1 = new ProgramWorkflowState();
		state1.setConcept(new Concept(3));
		state1.setTerminal(false);
		
		workflow.addState(state1);
		program.addWorkflow(workflow);
		
		pws.saveProgram(program);
	}
	
	@Test
	public void saveProgram_shouldFailIfProgramWorkFlowStateTerminalIsNull() {
		
		exception.expect(APIException.class);
		exception.expectMessage("ProgramWorkflowState concept, initial, terminal are required");
		
		Program program = new Program();
		program.setName("TEST PROGRAM");
		program.setDescription("TEST PROGRAM DESCRIPTION");
		program.setConcept(new Concept(1));
		
		ProgramWorkflow workflow = new ProgramWorkflow();
		workflow.setConcept(new Concept(2));
		
		ProgramWorkflowState state1 = new ProgramWorkflowState();
		state1.setConcept(new Concept(3));
		state1.setInitial(true);
		
		workflow.addState(state1);
		program.addWorkflow(workflow);
		
		pws.saveProgram(program);
	}
	
	@Test
	public void savePatientProgram_shouldFailForNullPatient() {
		
		exception.expect(APIException.class);
		exception.expectMessage("PatientProgram requires a Patient and a Program");
		
		PatientProgram patientProgram = new PatientProgram(1);
		patientProgram.setProgram(new Program(1));
		
		pws.savePatientProgram(patientProgram);
	}
	
	@Test
	public void savePatientProgram_shouldFailForNullProgram() {
		
		exception.expect(APIException.class);
		exception.expectMessage("PatientProgram requires a Patient and a Program");
		
		PatientProgram patientProgram = new PatientProgram(1);
		patientProgram.setPatient(new Patient(1));
		
		pws.savePatientProgram(patientProgram);
	}
	
	@Test
	public void purgePatientProgram_shouldFailGivenNonEmptyStatesAndTrueCascade() {
		
		exception.expect(APIException.class);
		exception.expectMessage("Cascade purging of PatientPrograms is not implemented yet");
		
		PatientProgram patientProgram = new PatientProgram();
		PatientState patientState = new PatientState();
		patientProgram.getStates().add(patientState);
		
		pws.purgePatientProgram(patientProgram, true);
	}
	
	@Test
	public void purgeProgram_shouldFailGivenNonEmptyWorkFlowsAndTrueCascade() {
		
		exception.expect(APIException.class);
		exception.expectMessage("Cascade purging of Programs is not implemented yet");
		
		Program program = new Program(1);
		ProgramWorkflow workflow = new ProgramWorkflow(1);
		program.addWorkflow(workflow);
		
		pws.purgeProgram(program, true);
	}
}

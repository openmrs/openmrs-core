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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
	
	
	@BeforeEach
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
		Mockito.when(mockDao.getProgramsByName("A name", false)).thenReturn(noProgramWithGivenName);
		Mockito.when(mockDao.getProgramsByName("A name", true)).thenReturn(noProgramWithGivenName);
		pws.setProgramWorkflowDAO(mockDao);
		assertNull(pws.getProgramByName("A name"));
	}
	
	@Test
	public void getProgramByName_shouldFailWhenTwoProgramsFoundWithSameName() {
		ProgramWorkflowDAO mockDao = Mockito.mock(ProgramWorkflowDAO.class);
		List<Program> programsWithGivenName = new ArrayList<>();
		Program program1 = new Program("A name");
		Program program2 = new Program("A name");
		programsWithGivenName.add(program1);
		programsWithGivenName.add(program2);
		Mockito.when(mockDao.getProgramsByName("A name", false)).thenReturn(programsWithGivenName);
		Mockito.when(mockDao.getProgramsByName("A name", true)).thenReturn(programsWithGivenName);
		pws.setProgramWorkflowDAO(mockDao);
		assertThrows(ProgramNameDuplicatedException.class, () -> pws.getProgramByName("A name"));
	}
	
	@Test
	public void saveProgram_shouldFailIfProgramConceptIsNull() {
		
		Program program1 = new Program(1);
		
		APIException exception = assertThrows(APIException.class, () -> pws.saveProgram(program1));
		assertThat(exception.getMessage(), is("Program concept is required"));
	}
	
	@Test
	public void saveProgram_shouldFailIfProgramWorkFlowConceptIsNull() {
		
		Program program = new Program();
		program.setName("TEST PROGRAM");
		program.setDescription("TEST PROGRAM DESCRIPTION");
		program.setConcept(new Concept(1));
		program.addWorkflow(new ProgramWorkflow());
		APIException exception = assertThrows(APIException.class, () -> pws.saveProgram(program));
		assertThat(exception.getMessage(), is("ProgramWorkflow concept is required"));
	}
	
	@Test
	public void saveProgram_shouldFailIfProgramWorkFlowStateConceptIsNull() {
		
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
		APIException exception = assertThrows(APIException.class, () -> pws.saveProgram(program));
		assertThat(exception.getMessage(), is("ProgramWorkflowState concept, initial, terminal are required"));
	}
	
	@Test
	public void saveProgram_shouldFailIfProgramWorkFlowStateInitialIsNull() {
		
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
		APIException exception = assertThrows(APIException.class, () ->  pws.saveProgram(program));
		assertThat(exception.getMessage(), is("ProgramWorkflowState concept, initial, terminal are required"));
	}
	
	@Test
	public void saveProgram_shouldFailIfProgramWorkFlowStateTerminalIsNull() {
		
		
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
		APIException exception = assertThrows(APIException.class, () -> pws.saveProgram(program));
		assertThat(exception.getMessage(), is("ProgramWorkflowState concept, initial, terminal are required"));
	}
	
	@Test
	public void savePatientProgram_shouldFailForNullPatient() {
		
		PatientProgram patientProgram = new PatientProgram(1);
		patientProgram.setProgram(new Program(1));
		APIException exception = assertThrows(APIException.class, () -> pws.savePatientProgram(patientProgram));
		assertThat(exception.getMessage(), is("PatientProgram requires a Patient and a Program"));
	}
	
	@Test
	public void savePatientProgram_shouldFailForNullProgram() {
		
		PatientProgram patientProgram = new PatientProgram(1);
		patientProgram.setPatient(new Patient(1));
		APIException exception = assertThrows(APIException.class, () -> pws.savePatientProgram(patientProgram));
		assertThat(exception.getMessage(), is("PatientProgram requires a Patient and a Program"));
	}
	
	@Test
	public void purgePatientProgram_shouldFailGivenNonEmptyStatesAndTrueCascade() {
		
		PatientProgram patientProgram = new PatientProgram();
		PatientState patientState = new PatientState();
		patientProgram.getStates().add(patientState);
		APIException exception = assertThrows(APIException.class, () -> pws.purgePatientProgram(patientProgram, true));
		assertThat(exception.getMessage(), is("Cascade purging of PatientPrograms is not implemented yet"));
	}
	
	@Test
	public void purgeProgram_shouldFailGivenNonEmptyWorkFlowsAndTrueCascade() {
		
		Program program = new Program(1);
		ProgramWorkflow workflow = new ProgramWorkflow(1);
		program.addWorkflow(workflow);
		APIException exception = assertThrows(APIException.class, () -> pws.purgeProgram(program, true));
		assertThat(exception.getMessage(), is("Cascade purging of Programs is not implemented yet"));
	}
}

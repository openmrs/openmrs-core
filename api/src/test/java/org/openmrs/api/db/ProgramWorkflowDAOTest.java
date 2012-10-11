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
package org.openmrs.api.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

import java.util.Date;
import java.util.List;

public class ProgramWorkflowDAOTest extends BaseContextSensitiveTest {
	
	private ProgramWorkflowDAO dao = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link org.openmrs.test.BaseContextSensitiveTest} is run right before this method.
	 *
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		// fetch the dao from the spring application context
		// this bean name matches the name in /metadata/spring/applicationContext-service.xml
		dao = (ProgramWorkflowDAO) applicationContext.getBean("programWorkflowDAO");
	}
	
	@Test
	@Verifies(value = "should get saved personAttributeType name from database", method = "saveProgram")
	public void saveProgram_shouldSaveProgram() throws Exception {
		Program program = createProgram();
		dao.saveProgram(program);
	}
	
	private Program createProgram() {
		Program program = new Program();
		program.setName("OpenMRS");
		program.setDescription("An opensource medical record system");
		program.setDateCreated(new Date());
		return program;
	}
	
	@Test
	@Verifies(value = "should assign a concept to a program and save it to the database", method = "saveProgram")
	public void saveProgram_shouldAlsoSaveOutcomesConcept() {
		Concept outcomesConcept = Context.getConceptService().getConcept(3);
		Program program = createProgram();
		program.setOutcomesConcept(outcomesConcept);
		int id = dao.saveProgram(program).getId();
		
		clearHibernateCache();
		Program savedProgram = dao.getProgram(id);
		Assert.assertEquals(3, savedProgram.getOutcomesConcept().getId().intValue());
	}
	
	@Test
	@Verifies(value = "should return an empty list when there is no program in the dB with given name", method = "getProgramsByName")
	public void getProgramsByName_whenThereAreNoProgramsWithTheGivenName_shouldReturnAnEmptyList() {
		Program program = createProgram();
		program.setName("wrongProgramName");
		dao.saveProgram(program);
		clearHibernateCache();
		List<Program> programs = dao.getProgramsByName("testProgramName", true);
		Assert.assertNotNull(programs);
		Assert.assertEquals(0, programs.size());
	}
	
	@Test
	@Verifies(value = "should return only and exactly the programs with the given name", method = "getProgramsByName")
	public void getProgramsByName_whenThereAreProgramsWithTheGivenName_shouldReturnAllProgramsWithTheGivenName() {
		Program program1 = createProgram();
		program1.setName("testProgramName");
		dao.saveProgram(program1);
		Program program2 = createProgram();
		program2.setName("testProgramName");
		dao.saveProgram(program2);
		Program program3 = createProgram();
		program3.setName("wrongProgramName");
		dao.saveProgram(program3);
		clearHibernateCache();
		List<Program> programs = dao.getProgramsByName("testProgramName", true);
		Assert.assertEquals(2, programs.size());
		Assert.assertEquals(program1, programs.get(0));
		Assert.assertEquals(program2, programs.get(1));
	}
	
}

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

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

public class ProgramWorkflowDAOTest extends BaseContextSensitiveTest {
	
	private ProgramWorkflowDAO dao = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link org.openmrs.test.BaseContextSensitiveTest} is run right before this method.
	 *
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() {
		// fetch the dao from the spring application context
		// this bean name matches the name in /metadata/spring/applicationContext-service.xml
		dao = (ProgramWorkflowDAO) applicationContext.getBean("programWorkflowDAO");
	}
	
	@Test
	public void saveProgram_shouldSaveProgram() {
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

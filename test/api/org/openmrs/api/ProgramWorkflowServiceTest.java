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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.openmrs.BaseContextSensitiveTest;
import org.openmrs.Patient;
import org.openmrs.Program;
import org.openmrs.api.context.Context;

/**
 *
 */
public class ProgramWorkflowServiceTest extends BaseContextSensitiveTest {

	protected static final String DATASET_XML = "org/openmrs/include/ProgramWorkflowServiceTest.xml";
	ProgramWorkflowService s;
	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	
	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		if (s == null) {
			s = Context.getProgramWorkflowService();
		}
	}
	
	public void testPatientsInProgram() throws Exception {	
		executeDataSet(DATASET_XML);
		Program hiv = s.getProgram(1);
		assertNotNull(hiv);
		Collection<Integer> temp = s.patientsInProgram(hiv, null, null);
		assertEquals(1, temp.size());
		temp = s.patientsInProgram(hiv, ymd.parse("1900-01-01"), ymd.parse("1914-01-01"));
		assertEquals(0, temp.size());
		temp = s.patientsInProgram(hiv, ymd.parse("1900-01-01"), ymd.parse("2006-04-11"));
		assertEquals(1, temp.size());
	}
	
	public void testIsInProgram() throws Exception {
		executeDataSet(DATASET_XML);
		Patient yes = Context.getPatientService().getPatient(3);
		Patient no = Context.getPatientService().getPatient(2);
		Program hiv = s.getProgram(1);
		assertNotNull(hiv);
		assertTrue(s.isInProgram(yes, hiv, null, null));
		assertFalse(s.isInProgram(no, hiv, null, null));
	}
	
	public void testEnrollInProgram() throws Exception {
		executeDataSet(DATASET_XML);
		Patient p = Context.getPatientService().getPatient(2);
		Program hiv = s.getProgram(1);
		assertFalse(s.isInProgram(p, hiv, null, null));
		s.enrollPatientInProgram(p, hiv, new Date(), null);
		assertTrue(s.isInProgram(p, hiv, null, null));
	}
	
}

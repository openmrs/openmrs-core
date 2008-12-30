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
package org.openmrs.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.result.Result;

/**
 * Tests the ObsDataSource functionality
 */
public class ObsDataSourceTest extends LogicBaseContextSensitiveTest {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Before
	public void runBeforeEachTest() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/logic/include/ObsDataSourceTest.xml");
		authenticate();
	}
	
	/**
	 * TODO change to use the in memory database
	 */
	@Test
	public void shouldObsDataSource() throws Exception {
		LogicDataSource lds = Context.getLogicService().getLogicDataSource("obs");
		Cohort patients = new Cohort();
		
		patients.addMember(2);
		patients.addMember(3);
		
		assertEquals(2, patients.getSize());
		LogicContext context = new LogicContext(patients);
		Map<Integer, Result> result = lds.read(context, patients, new LogicCriteria("CD4 COUNT"));
		context = null;
		assertNotNull(result);
		assertEquals(2, result.size());
		
		for (Integer id : result.keySet()) {
			for (Result r : result.get(id)) {
				log.error("PatientID: " + id + ", CD4 COUNT: " + r.toNumber());
			}
		}
	}
}

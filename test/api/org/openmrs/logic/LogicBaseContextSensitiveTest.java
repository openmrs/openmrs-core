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

import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.impl.LogicServiceImpl;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * All logic tests should extend this class so that concepts are re-registered correctly for each
 * test method.
 */
@Ignore
// so that junit doesn't try to run this like a test
public abstract class LogicBaseContextSensitiveTest extends BaseContextSensitiveTest {
	
	/**
	 * This is run before every test method in every class that extends this class. This is slightly
	 * hacky, but it empties out the LogicService class so that the RuleFactory is rebuilt so it can
	 * register new concepts as tokens.
	 * 
	 * @throws Exception
	 */
	@Before
	public void resetLogicServiceTokens() throws Exception {
		// get the data sources from the current logic service
		Map<String, LogicDataSource> datasources = Context.getLogicService().getLogicDataSources();
		
		// hacky part by just doing newLogicServiceImpl();
		// create a new logic service and put it on the Context over the old one
		LogicService newLogicService = new LogicServiceImpl();
		newLogicService.setLogicDataSources(datasources);
		ServiceContext.getInstance().setLogicService(newLogicService);
	}
	
}

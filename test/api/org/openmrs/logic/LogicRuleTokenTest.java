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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 *
 */
public class LogicRuleTokenTest extends BaseContextSensitiveTest {
	
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet("org/openmrs/logic/include/LogicStandardDatasets.xml");
	}
	
	@Test
	public void shouldSaveLogicToken() throws Exception {
		LogicService logicService = Context.getLogicService();
		
		Rule rule = logicService.getRule("%%person.death date");
		logicService.addRule("DEATH DATE", rule);
		
		rule = logicService.getRule("DEATH DATE");
		Assert.assertNotNull(rule);
		
		logicService.addTokenTag("DEATH DATE", "aids");
		logicService.addTokenTag("DEATH DATE", "hiv");
		logicService.updateRule("DEATH DATE", rule);
		rule = logicService.getRule("DEATH DATE");
		Assert.assertEquals(logicService.getTokenTags("DEATH DATE").size(), 2);
	}
	
	@Test(expected = LogicException.class)
	public void shouldGiveLogicException() throws Exception {
		LogicService logicService = Context.getLogicService();
		
		Rule rule = logicService.getRule("%%person.birthdate");
		logicService.addRule("BIRTHDATE", rule);
		logicService.removeRule("BIRTHDATE");
		
		rule = logicService.getRule("BIRTHDATE");
		Assert.assertNull(rule);
	}
}

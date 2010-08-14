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
package org.openmrs.module;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.util.Assert;

/**
 * Tests for the {@link ModuleRepository} for correct functioning of repository methods
 */
public class ModuleRepositoryTest extends BaseContextSensitiveTest {
	
	private static final Log log = LogFactory.getLog(ModuleRepositoryTest.class);
	
	/**
	 * Tests whether an empty list is returned if search is empty
	 */
	@Test
	@Verifies(value = "return an empty array list of modules if search is empty", method = "searchModules(String)")
	public void searchModules_returnAnEmptyArrayListOfModulesIfSearchIsEmpty() {
		List<Module> matchingModules = ModuleRepository.searchModules("");
		Assert.notNull(matchingModules);
	}
}

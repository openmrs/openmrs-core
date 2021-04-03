/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.annotation;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.test.StartModule;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

@StartModule({"org/openmrs/module/include/test1-1.0-SNAPSHOT.omod"})
public class OpenmrsProfileExcludeFilterWithModulesTest extends BaseContextSensitiveTest {
	/**
	 * @see OpenmrsProfileExcludeFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
	 */
	@Test
	public void match_shouldNotIncludeBeanIfModuleIsStarted() {
		assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(OpenmrsProfileWithoutTest1Module.class));
	}
}

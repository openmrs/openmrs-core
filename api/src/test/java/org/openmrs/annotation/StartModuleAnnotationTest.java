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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.test.StartModule;

@StartModule({ "org/openmrs/module/include/test1-1.0-SNAPSHOT.omod", "org/openmrs/module/include/test2-1.0-SNAPSHOT.omod" })
public class StartModuleAnnotationTest extends BaseContextSensitiveTest {
	
	@Test
	public void shouldStartModules() throws ClassNotFoundException {
		
		Class<?> test1ServiceClass = Context.loadClass("org.openmrs.module.test1.api.Test1Service");
		Class<?> test2ServiceClass = Context.loadClass("org.openmrs.module.test2.api.Test2Service");
		assertNotNull(test1ServiceClass);
		assertNotNull(test2ServiceClass);
		
		assertNotNull(Context.getService(test1ServiceClass));
		assertNotNull(Context.getService(test2ServiceClass));
	}
}

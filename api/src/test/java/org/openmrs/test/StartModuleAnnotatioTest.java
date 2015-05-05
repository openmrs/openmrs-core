/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test;

import junit.framework.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;

@StartModule( { "org/openmrs/module/include/dssmodule-1.44.omod", "org/openmrs/module/include/atdproducer-0.51.omod" })
public class StartModuleAnnotatioTest extends BaseModuleContextSensitiveTest {
	
	@Test
	public void shouldStartModules() throws Exception {
		
		Class<?> atdServiceClass = Context.loadClass("org.openmrs.module.atdproducer.service.ATDService");
		Class<?> dssServiceClass = Context.loadClass("org.openmrs.module.dssmodule.DssService");
		Assert.assertNotNull(atdServiceClass);
		Assert.assertNotNull(dssServiceClass);
		
		Assert.assertNotNull(Context.getService(atdServiceClass));
		Assert.assertNotNull(Context.getService(dssServiceClass));
	}
}

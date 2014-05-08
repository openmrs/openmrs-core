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

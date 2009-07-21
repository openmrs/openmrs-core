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

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.SerializedObjectDAOTest;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleInteroperabilityTest;
import org.openmrs.module.ModuleUtil;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * To use this annotation:
 * <ol>
 * <li>Add the @StartModule( { "/path/to/your/module.omod" } ) annotation to the test class
 * <li>Add the @DirtiesContext method to the last test method in the class
 * </ol>
 * 
 * @see SerializedObjectDAOTest
 * @see ModuleInteroperabilityTest
 * @see BaseContextSensitiveTest
 */
public class StartModuleExecutionListener extends AbstractTestExecutionListener {
	
	// there is one ExecutionListener instance per class, so this instance variable works well for us
	private boolean springAlreadyRestartedOnce = false;
	
	/**
	 * called before @BeforeTransaction methods
	 * 
	 * @see org.springframework.test.context.support.AbstractTestExecutionListener#prepareTestInstance(org.springframework.test.context.TestContext)
	 */
	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception {
		StartModule startModuleAnnotation = testContext.getTestClass().getAnnotation(StartModule.class);
		
		// if the developer listed some modules with the @StartModule annotation on the class
		if (startModuleAnnotation != null) {
			
			if (!springAlreadyRestartedOnce) {
				if (!Context.isSessionOpen())
					Context.openSession();
				
				// load the omod
				String modulesToLoad = StringUtils.join(startModuleAnnotation.value(), " ");
				Properties props = new Properties();
				props.setProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD, modulesToLoad);
				ModuleUtil.startup(props);
				Assert.assertEquals("Some of the modules did not start successfully", startModuleAnnotation.value().length,
				    ModuleFactory.getStartedModules().size());
				
				// refresh spring so the Services are recreated (aka serializer gets put into the SerializationService)
				new ClassPathXmlApplicationContext(new String[] { "applicationContext-service.xml",
				        "classpath*:moduleApplicationContext.xml" });
				
				// session is closed by the test framework
				//Context.closeSession();
				
				// mark this as true so that the services are only restarted once
				springAlreadyRestartedOnce = true;
			}
		}
	}
	
}

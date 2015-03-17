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

import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.SerializedObjectDAOTest;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleInteroperabilityTest;
import org.openmrs.module.ModuleUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * To use this annotation, add the @StartModule( { "/path/to/your/module.omod" } ) annotation to the
 * test class
 * 
 * @see SerializedObjectDAOTest
 * @see ModuleInteroperabilityTest
 * @see BaseContextSensitiveTest
 */
public class StartModuleExecutionListener extends AbstractTestExecutionListener {
	
	// stores the last class that restarted the module system because we only 
	// want it restarted once per class, not once per method
	private static String lastClassRun = "";
	
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
			
			if (!lastClassRun.equals(testContext.getTestClass().getSimpleName())) {
				// mark this with our class so that the services are only restarted once
				lastClassRun = testContext.getTestClass().getSimpleName();
				
				if (!Context.isSessionOpen())
					Context.openSession();
				
				ModuleUtil.shutdown();
				
				// load the omods that the dev defined for this class
				String modulesToLoad = StringUtils.join(startModuleAnnotation.value(), " ");
				
				Properties props = BaseContextSensitiveTest.runtimeProperties;
				props.setProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD, modulesToLoad);
				try {
					ModuleUtil.startup(props);
				}
				catch (Exception e) {
					System.out.println("Error while starting modules: ");
					e.printStackTrace(System.out);
					throw e;
				}
				Assert.assertTrue("Some of the modules did not start successfully for "
				        + testContext.getTestClass().getSimpleName() + ". Only " + ModuleFactory.getStartedModules().size()
				        + " modules started instead of " + startModuleAnnotation.value().length, startModuleAnnotation
				        .value().length <= ModuleFactory.getStartedModules().size());
				
				/*
				 * Refresh spring so the Services are recreated (aka serializer gets put into the SerializationService)
				 * To do this, wrap the applicationContext from the testContext into a GenericApplicationContext, allowing
				 * loading beans from moduleApplicationContext into it and then calling ctx.refresh()
				 * This approach ensures that the application context remains consistent
				 */
				GenericApplicationContext ctx = new GenericApplicationContext(testContext.getApplicationContext());
				XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
				
				Enumeration<URL> list = OpenmrsClassLoader.getInstance().getResources("moduleApplicationContext.xml");
				while (list.hasMoreElements()) {
					xmlReader.loadBeanDefinitions(new UrlResource(list.nextElement()));
				}
				
				//ensure that when refreshing, we use the openmrs class loader for the started modules.
				boolean useSystemClassLoader = Context.isUseSystemClassLoader();
				Context.setUseSystemClassLoader(false);
				try {
					ctx.refresh();
				}
				finally {
					Context.setUseSystemClassLoader(useSystemClassLoader);
				}
				
				// session is closed by the test framework
				//Context.closeSession();
			}
		}
	}
	
	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		StartModule startModuleAnnotation = testContext.getTestClass().getAnnotation(StartModule.class);
		
		if (startModuleAnnotation != null) {
			if (!Context.isSessionOpen()) {
				Context.openSession();
			}
			
			ModuleUtil.shutdown();
			
			Context.closeSession();
		}
	}
}

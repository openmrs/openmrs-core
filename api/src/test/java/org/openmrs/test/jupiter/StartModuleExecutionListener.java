/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test.jupiter;

import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.SerializedObjectDAOTest;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleInteroperabilityTest;
import org.openmrs.module.ModuleUtil;
import org.openmrs.test.StartModule;
import org.openmrs.util.OpenmrsClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
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
 * 
 * @since 2.4.0
 */
class StartModuleExecutionListener extends AbstractTestExecutionListener {
	
	private static final Logger log = LoggerFactory.getLogger(StartModuleExecutionListener.class);
	
	// stores the last class that restarted the module system because we only 
	// want it restarted once per class, not once per method
	private static String lastClassRun = "";
	
	// storing the bean definitions that have been manually removed from the context
	private Map<String, BeanDefinition> filteredDefinitions = new HashMap<>();
	
	/**
	 * called before @BeforeTransaction methods
	 * 
	 * @see AbstractTestExecutionListener#prepareTestInstance(TestContext)
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
					log.error("Error while starting modules: ", e);
					throw e;
				}
				assertTrue("Some of the modules did not start successfully for "
					+ testContext.getTestClass().getSimpleName() + ". Only " + ModuleFactory.getStartedModules().size()
					+ " modules started instead of " + startModuleAnnotation.value().length, startModuleAnnotation
					.value().length <= ModuleFactory.getStartedModules().size());
				
				/*
				 * Refresh spring so the Services are recreated (aka serializer gets put into the SerializationService)
				 * To do this, wrap the applicationContext from the testContext into a GenericApplicationContext, allowing
				 * loading beans from moduleApplicationContext into it and then calling ctx.refresh()
				 * This approach ensures that the application context remains consistent
				 */
				removeFilteredBeanDefinitions(testContext.getApplicationContext());
				GenericApplicationContext ctx = new GenericApplicationContext(testContext.getApplicationContext());
				
				XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
				Enumeration<URL> list = OpenmrsClassLoader.getInstance().getResources("moduleApplicationContext.xml");
				while (list.hasMoreElements()) {
					xmlReader.loadBeanDefinitions(new UrlResource(list.nextElement()));
				}
				
				Context.setUseSystemClassLoader(false);
				ctx.refresh();
			}
		}
	}
	
	/*
	 * Starting modules may require to remove beans definitions that were initially loaded.
	 */
	protected void removeFilteredBeanDefinitions(ApplicationContext context) {
		// first looking at a context loading the bean definitions "now"
		GenericApplicationContext ctx = new GenericApplicationContext();
		(new XmlBeanDefinitionReader(ctx)).loadBeanDefinitions("classpath:applicationContext-service.xml");
		Set<String> filteredBeanNames = new HashSet<>();
		for (String beanName : ctx.getBeanDefinitionNames()) {
			if (beanName.startsWith("openmrsProfile")) {
				filteredBeanNames.add(beanName);
			}
		}
		ctx.close();
		
		// then looking at the context as it loaded the bean definitions before the module(s) were started
		Set<String> originalBeanNames = new HashSet<>();
		for (String beanName : ((GenericApplicationContext) context).getBeanDefinitionNames()) {
			if (beanName.startsWith("openmrsProfile")) {
				originalBeanNames.add(beanName);
			}
		}
		// removing the bean definitions that have been filtered out by starting the module(s)
		for (String beanName : originalBeanNames) {
			if (!filteredBeanNames.contains(beanName)) {
				filteredDefinitions.put(beanName, ((GenericApplicationContext) context).getBeanDefinition(beanName));
				((GenericApplicationContext) context).removeBeanDefinition(beanName);
			}
		}
	}
	
	@Override
	public void afterTestClass(TestContext testContext) {
		StartModule startModuleAnnotation = testContext.getTestClass().getAnnotation(StartModule.class);
		
		if (startModuleAnnotation != null) {
			if (!Context.isSessionOpen()) {
				Context.openSession();
			}
			
			// re-registering the bean definitions that we may have removed
			for (String beanName : filteredDefinitions.keySet()) {
				((GenericApplicationContext) testContext.getApplicationContext())
					.registerBeanDefinition(beanName, filteredDefinitions.get(beanName));
			}
			filteredDefinitions.clear();
			
			ModuleUtil.shutdown();
			
			Context.closeSession();
		}
	}
}

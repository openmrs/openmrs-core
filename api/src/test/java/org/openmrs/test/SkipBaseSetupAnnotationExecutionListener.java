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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * This class is added to all {@link BaseContextSensitiveTest} classes due to being in the list of
 * "@TestExecutionListeners" annotation. This looks for the "@SkipInitializationData",
 * "@SkipStandardData" and "@SkipAuthorization" annotations on the current test method and skips the
 * associated action in the {@link BaseContextSensitiveTest#setupDatabaseWithStandardData()} method
 * 
 * @see SkipBaseSetup
 * @see BaseContextSensitiveTest
 */
public class SkipBaseSetupAnnotationExecutionListener extends AbstractTestExecutionListener {
	
	/**
	 * This method is run before all "@Before" methods thanks to Spring and the
	 * "@TestExecutionListeners" annotation on the {@link BaseContextSensitiveTest} class.
	 * 
	 * @see org.springframework.test.context.support.AbstractTestExecutionListener#beforeTestMethod(org.springframework.test.context.TestContext)
	 */
	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		Method testMethod = testContext.getTestMethod();
		
		Annotation skipBaseSetup = testMethod.getAnnotation(SkipBaseSetup.class);
		
		// if the method doesn't have the annotation, check the class
		if (skipBaseSetup == null)
			skipBaseSetup = testContext.getTestClass().getAnnotation(SkipBaseSetup.class);
		
		// if the annotation exists, call BaseContextSensitiveTest#skipBaseSetup()
		// so that the method calls in baseSetupWithStandardDataAndAuthentication()
		// are not run.
		if (skipBaseSetup != null) {
			callMethod(testContext, "skipBaseSetup");
		}
		
	}
	
	/**
	 * Convenience method to call the given method on the current test class as denoted by the
	 * TestContext
	 * 
	 * @param testContext current context that has the current test class/method on it
	 * @param methodName the name of the method to invoke
	 * @throws Exception
	 */
	private void callMethod(TestContext testContext, String methodName) throws Exception {
		
		Method method = null;
		
		try {
			method = testContext.getTestClass().getMethod(methodName);
		}
		catch (NoSuchMethodException e) {
			throw new Exception("There is no method named '" + methodName + "' on the " + testContext.getTestClass()
			        + " class", e);
		}
		
		method.invoke(testContext.getTestInstance());
	}
	
}

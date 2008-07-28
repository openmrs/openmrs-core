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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.openmrs.util.OpenmrsUtil;

/**
 * Runs tests on the openmrs junit tests
 */
public class OpenmrsTestsTest extends TestCase {
	
	private ClassLoader classLoader = this.getClass().getClassLoader();
	private List<Class<TestCase>> testClasses = null;
	
	/**
	 * Make sure there is at least one _other_ test case out there
	 * 
	 * @throws Exception
	 */
	public void testShouldHaveAtLeastOneTest() throws Exception {
		List<Class<TestCase>> classes = getTestClasses();
		
		assertTrue("There should be more than one class but there was only " + classes.size(), classes.size() > 1);
	}
	
	/**
	 * Makes sure all methods in org.openmrs.test that start with
	 * "test" actually start with "testShould" 
	 * 
	 * @throws Exception
	 */
	public void testShouldStartWithTestShould() throws Exception {
		
		List<Class<TestCase>> classes = getTestClasses();
		
		for (Class<TestCase> currentClass : classes) {
			for (Method method : currentClass.getMethods()) {
	    		String methodName = method.getName();
	    		
	    		// make sure every "test" method starts with "testShould"
	    		if (methodName.startsWith("test")) {
	    			assertTrue(currentClass.getName() + "#" + methodName + " is supposed to start with 'testShould' but it doesn't", methodName.startsWith("testShould"));
	    		}
	    	}
		}
		
	}
	
	/**
	 * Get all classes in the org.openmrs.test package
	 * 
	 * @return list of TestCase classes in org.openmrs.test
	 */
	private List<Class<TestCase>> getTestClasses() {
		if (testClasses != null)
			return testClasses;
		
		URL url = classLoader.getResource("org/openmrs/test");
		File directory = OpenmrsUtil.url2file(url);
		// make sure we get a directory back
		assertTrue("org.openmrs.test should be a directory", directory.isDirectory());
		
		testClasses = getTestClassesInDirectory(directory);
		
		url = classLoader.getResource("org/openmrs/web/test");
		directory = OpenmrsUtil.url2file(url);
		// make sure we get a directory back
		assertTrue("org.openmrs.web.test should be a directory", directory.isDirectory());
		
		testClasses.addAll(getTestClassesInDirectory(directory));
		
		return testClasses;
	}
	
	/**
	 * Recurses into the given directory checking that all test 
	 * methods start with "testShould"
	 * 
	 * @param directory to loop through the files of
	 */
	@SuppressWarnings("unchecked")
    private List<Class<TestCase>> getTestClassesInDirectory(File directory) {
		
		List<Class<TestCase>> currentDirTestClasses = new ArrayList<Class<TestCase>>();
		
		for (File currentFile : directory.listFiles()) {
			
			// if looking at a folder, recurse into it
			if (currentFile.isDirectory()) {
				currentDirTestClasses.addAll(getTestClassesInDirectory(currentFile));
			}
			
			if (currentFile.getName().endsWith("class")) {
				// strip off the ending
				String className = currentFile.getAbsolutePath().replace(".class", "");
				
				// switch to dot separation
				className = className.replace("/", ".");
				
				// strip out the beginning up to org.openmrs.
				className = className.substring(className.indexOf("org.openmrs."));
				
				try {
	                Class<?> currentClass = classLoader.loadClass(className);
	                
	                // if the class is a TestCase, put it in the current list of classes
	                // if we change to junit4, this will probably have to change
	                if (TestCase.class.isAssignableFrom(currentClass)) {
	                	currentDirTestClasses.add((Class<TestCase>)currentClass);
	                }
	                
                } catch (ClassNotFoundException e) {
	               System.out.println("Unable to load class: " + className + " error: " + e.getMessage());
                }
			}
		}
		
		return currentDirTestClasses;
	}
	
}
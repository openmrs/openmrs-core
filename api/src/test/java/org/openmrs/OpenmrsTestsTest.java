/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.util.OpenmrsUtil;

/**
 * Runs tests on the openmrs junit tests TODO: add unit test to make sure all tests have a call to
 * assert* in them. This would help prevent people from making tests that just print results to the
 * screen
 */
@SuppressWarnings("unchecked")
public class OpenmrsTestsTest {
	
	private ClassLoader classLoader = this.getClass().getClassLoader();
	
	private List<Class> testClasses = null;
	
	/**
	 * Make sure there is at least one _other_ test case out there
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldHaveAtLeastOneTest() throws Exception {
		List<Class> classes = getTestClasses();
		
		assertTrue("There should be more than one class but there was only " + classes.size(), classes.size() > 1);
	}
	
	/**
	 * Makes sure all test methods in org.openmrs start with the word "should"
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldStartWithShould() throws Exception {
		
		List<Class> classes = getTestClasses();
		
		for (Class<TestCase> currentClass : classes) {
			for (Method method : currentClass.getMethods()) {
				
				// make sure every "test" method (determined by having 
				// the @Test annotation) starts with "testShould"
				if (method.getAnnotation(Test.class) != null) {
					String methodName = method.getName();
					
					boolean passes = methodName.startsWith("should") || methodName.contains("_should");
					assertTrue(currentClass.getName() + "#" + methodName
					        + " is supposed to either 1) start with 'should' or 2) contain '_should' but it doesn't", passes);
				}
			}
		}
	}
	
	/**
	 * Makes sure all "should___" methods in org.openmrs have an "@Test" annotation on it. This is
	 * to help prevent devs from forgetting to put the annotation and then seeing all tests pass
	 * because the new test wasn't actually ran
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldHaveTestAnnotationWhenStartingWithShould() throws Exception {
		// loop over all methods in all test classes
		for (Class<TestCase> currentClass : getTestClasses()) {
			for (Method method : currentClass.getMethods()) {
				String methodName = method.getName();
				
				// make sure every should___ method has an @Test annotation
				if (methodName.startsWith("should") || methodName.contains("_should")) {
					assertTrue(currentClass.getName() + "#" + methodName
					        + " does not have the @Test annotation on it even though the method name starts with 'should'",
					    method.getAnnotation(Test.class) != null);
				}
			}
		}
	}
	
	/**
	 * Checks that a user hasn't accidentally created a test class that doesn't end with "Test". (If
	 * it doesn't, it isn't picked up by the test aggregator Ant target: junit-report) <br/>
	 * <br/>
	 * This class looks at all classes in the org.openmrs package. If a class contains an "@Test"
	 * annotated method but its class name does not end with Test, it fails.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldHaveClassNameEndWithTestIfContainsMethodTestAnnotations() throws Exception {
		// loop over all methods that _don't_ end in Test.class
		for (Class<?> currentClass : getClasses("^.*(?<!Test|IT|PT)\\.class$")) {
			
			// skip over classes that are @Ignore'd
			if (currentClass.getAnnotation(Ignore.class) == null) {
				boolean foundATestMethod = false;
				
				for (Method method : currentClass.getMethods()) {
					if (method.getAnnotation(Test.class) != null) {
						foundATestMethod = true;
					}
				}
				
				Assert.assertFalse(
				    currentClass.getName() + " does not end with 'Test' but contains @Test annotated methods",
				    foundATestMethod);
			}
		}
	}
	
	/**
	 * Get all classes ending in "Test.class".
	 * 
	 * @return list of classes whose name ends with Test.class
	 */
	private List<Class> getTestClasses() {
		return getClasses(".*(Test|IT|PT)\\.class$");
	}
	
	/**
	 * Get all classes in the org.openmrs.test package
	 * 
	 * @return list of TestCase classes in org.openmrs.test
	 */
	private List<Class> getClasses(String classNameRegex) {
		if (testClasses != null)
			return testClasses;
		
		Pattern pattern = Pattern.compile(classNameRegex);
		
		URL url = classLoader.getResource("org/openmrs");
		File directory = OpenmrsUtil.url2file(url);
		// make sure we get a directory back
		assertTrue("org.openmrs.test should be a directory", directory.isDirectory());
		
		testClasses = getClassesInDirectory(directory, pattern);
		
		// check to see if the web layer is also included.  Skip it if its not there
		url = classLoader.getResource("org/openmrs/web");
		if (url != null) {
			directory = OpenmrsUtil.url2file(url);
			// make sure we get a directory back
			assertTrue("org.openmrs.web.test should be a directory", directory.isDirectory());
			
			testClasses.addAll(getClassesInDirectory(directory, pattern));
		}
		
		return testClasses;
	}
	
	/**
	 * Recurses into the given directory checking that all test methods start with "testShould"
	 * 
	 * @param directory to loop through the files of
	 */
	private List<Class> getClassesInDirectory(File directory, Pattern pattern) {
		
		List<Class> currentDirTestClasses = new ArrayList<Class>();
		
		for (File currentFile : directory.listFiles()) {
			
			// if looking at a folder, recurse into it
			if (currentFile.isDirectory()) {
				currentDirTestClasses.addAll(getClassesInDirectory(currentFile, pattern));
			}
			
			// if the user only wants classes ending in Test or they want all of them
			if (pattern.matcher(currentFile.getName()).matches()) {
				// strip off the extension
				String className = currentFile.getAbsolutePath().replace(".class", "");
				
				// switch to dot separation
				className = className.replace(File.separator, ".");
				
				// strip out the beginning (/home/ben/workspace...) up to org.openmrs.
				className = className.substring(className.lastIndexOf("org.openmrs."));
				
				try {
					Class<?> currentClass = classLoader.loadClass(className);
					
					currentDirTestClasses.add(currentClass);
					
				}
				catch (ClassNotFoundException e) {
					System.out.println("Unable to load class: " + className + " error: " + e.getMessage());
				}
			}
		}
		
		return currentDirTestClasses;
	}
	
}

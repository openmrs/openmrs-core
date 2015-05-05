/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Unit Test class to ensure that none of the DWR services have deprecated methods.
 * For reasoning behind this see JIRA issue:
 * https://tickets.openmrs.org/browse/TRUNK-2517
 *
 */
public class DeprecationCheckTest {
	
	private static final String OPENMRS_DWR_PACKAGE_NAME = "org.openmrs.web.dwr";
	
	/**
	 * @verifies fail if any of the DWR*Service classes contain @Deprecated annotation (TRUNK-2517)
	 */
	@Test
	public void checkThatNoDeprecatedMethodExistsInServiceClassesInDWRPackage() {
		try {
			List<String> candidates = findDWRServiceClassesWhichContainDeprecatedAnnotation();
			if (candidates.size() > 0) {
				String message = "Found classes in DWR package which contain @Deprecated annotation. "
				        + "Deprecation of DWR classes/methods is not allowed. You should just go ahead and modify/delete the method. "
				        + "Please check the following classes: ";
				for (String className : candidates) {
					message += className + ",";
				}
				message = message.substring(0, message.length() - 1);
				fail(message);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns a list of class names which contain the @Deprecated annotation. Does this search ONLY for
	 * DWR*Service classes.
	 *
	 * Found the basic code here:
	 * http://stackoverflow.com/questions/1456930/how-do-i-read-all-classes-from-a-java-package-in-the-classpath
	 *
	 * @return List of classes which contain the Deprecated annotation (@Deprecated)
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private List<String> findDWRServiceClassesWhichContainDeprecatedAnnotation() throws IOException, ClassNotFoundException {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
		
		//Search only for Service Classes in DWR package.
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
		        + resolveBasePackage(OPENMRS_DWR_PACKAGE_NAME) + "/**/*Service.class";
		
		List<String> candidateClasses = new ArrayList<String>();
		Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
		for (Resource resource : resources) {
			if (resource.isReadable()) {
				MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
				if (doesClassContainDeprecatedAnnotation(metadataReader)) {
					candidateClasses.add(metadataReader.getClassMetadata().getClassName());
				}
			}
		}
		
		return candidateClasses;
	}
	
	private String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
	}
	
	/**
	 * For the given class, checks if it contains any @Deprecated annotation (at method/class level).
	 * @param metadataReader
	 * @return true if it finds @Deprecated annotation in the class or any of its methods.
	 * @throws ClassNotFoundException
	 */
	private boolean doesClassContainDeprecatedAnnotation(MetadataReader metadataReader) throws ClassNotFoundException {
		try {
			Class dwrClass = Class.forName(metadataReader.getClassMetadata().getClassName());
			
			if (dwrClass.isAnnotationPresent(Deprecated.class)) {
				return true;
			}
			
			Method[] methods = dwrClass.getDeclaredMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(Deprecated.class))
					return true;
			}
		}
		catch (Throwable e) {}
		return false;
	}
}

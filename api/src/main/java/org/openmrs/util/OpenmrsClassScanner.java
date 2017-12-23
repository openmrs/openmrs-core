/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

/**
 * Reflection utilities to search the classpath for classes that have a given annotation, implement
 * a given interface, etc
 * 
 * @since 1.10
 */
public class OpenmrsClassScanner {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsClassScanner.class);
	
	private final MetadataReaderFactory metadataReaderFactory;
	
	private final ResourcePatternResolver resourceResolver;
	
	private Map<Class<?>, Set<Class<?>>> annotationToClassMap;
	
	private OpenmrsClassScanner() {
		this.metadataReaderFactory = new SimpleMetadataReaderFactory(OpenmrsClassLoader.getInstance());
		this.resourceResolver = new PathMatchingResourcePatternResolver(OpenmrsClassLoader.getInstance());
	}
	
	/**
	 * @return the instance
	 */
	public static OpenmrsClassScanner getInstance() {
		if (OpenmrsClassScannerHolder.INSTANCE == null) {
			OpenmrsClassScannerHolder.INSTANCE = new OpenmrsClassScanner();
		}
		
		return OpenmrsClassScannerHolder.INSTANCE;
	}
	
	public static void destroyInstance() {
		OpenmrsClassScannerHolder.INSTANCE = null;
	}
	
	/**
	 * Searches for classes with a given annotation.
	 * 
	 * @param annotationClass the annotation class
	 * @return the list of found classes
	 */
	public Set<Class<?>> getClassesWithAnnotation(Class annotationClass) {
		
		if (annotationToClassMap != null) {
			if (annotationToClassMap.containsKey(annotationClass)) {
				return annotationToClassMap.get(annotationClass);
			}
		} else {
			annotationToClassMap = new HashMap<>();
		}
		
		Set<Class<?>> types = new HashSet<>();
		String pattern = "classpath*:org/openmrs/**/*.class";
		
		try {
			Resource[] resources = resourceResolver.getResources(pattern);
			TypeFilter typeFilter = new AnnotationTypeFilter(annotationClass);
			for (Resource resource : resources) {
				try {
					MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
					if (typeFilter.match(metadataReader, metadataReaderFactory)) {
						String classname = metadataReader.getClassMetadata().getClassName();
						try {
							Class<?> metadata = (Class<?>) OpenmrsClassLoader.getInstance().loadClass(classname);
							types.add(metadata);
						}
						catch (ClassNotFoundException e) {
							throw new IOException("Class cannot be loaded: " + classname, e);
						}
					}
				}
				catch (IOException e) {
					log.debug("Resource cannot be loaded: " + resource);
				}
			}
		}
		catch (IOException ex) {
			log.error("Failed to look for classes with annocation" + annotationClass, ex);
		}
		
		annotationToClassMap.put(annotationClass, types);
		
		return types;
	}
	
	/**
	 * Private class to hold the one class scanner used throughout openmrs. This is an alternative
	 * to storing the instance object on {@link OpenmrsClassScanner} itself so that garbage
	 * collection can happen correctly.
	 */
	private static class OpenmrsClassScannerHolder {

		private OpenmrsClassScannerHolder() {
		}
		
		private static OpenmrsClassScanner INSTANCE = null;
	}
}

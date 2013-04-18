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
package org.openmrs.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private final MetadataReaderFactory metadataReaderFactory;
	
	private final ResourcePatternResolver resourceResolver;
	
	private Map<Class<?>, List<Class<?>>> annotationToClassMap;
	
	OpenmrsClassScanner() {
		this.metadataReaderFactory = new SimpleMetadataReaderFactory(OpenmrsClassLoader.getInstance());
		this.resourceResolver = new PathMatchingResourcePatternResolver(OpenmrsClassLoader.getInstance());
	}
	
	/**
	 * @return the instance
	 */
	public static OpenmrsClassScanner getInstance() {
		if (OpenmrsClassScannerHolder.INSTANCE == null)
			OpenmrsClassScannerHolder.INSTANCE = new OpenmrsClassScanner();
		
		return OpenmrsClassScannerHolder.INSTANCE;
	}
	
	public static void destroyInstance() {
		OpenmrsClassScannerHolder.INSTANCE = null;
	}
	
	/**
	 * Searches for classes with a given annotation.
	 * 
	 * @param annotation the annotation
	 * @return the list of found classes
	 */
	public List<Class<?>> getClassesWithAnnotation(Class annotationClass) {
		
		if (annotationToClassMap != null) {
			if (annotationToClassMap.containsKey(annotationClass)) {
				return annotationToClassMap.get(annotationClass);
			}
		} else {
			annotationToClassMap = new HashMap<Class<?>, List<Class<?>>>();
		}
		
		List<Class<?>> types = new ArrayList<Class<?>>();
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
							@SuppressWarnings("unchecked")
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
	 * Private class to hold the one class scanner used throughout openmrs. This is an alternative to
	 * storing the instance object on {@link OpenmrsClassScanner} itself so that garbage collection
	 * can happen correctly.
	 */
	private static class OpenmrsClassScannerHolder {
		
		private static OpenmrsClassScanner INSTANCE = null;
	}
}

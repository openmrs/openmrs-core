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
package org.openmrs.annotation;

import java.io.IOException;
import java.util.Map;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * Creates a bean if profile is matched. It returns true if a bean should be created.
 */
public class OpenmrsProfileIncludeFilter implements TypeFilter {
	
	private OpenmrsProfileExcludeFilter openmrsProfileExcludeFilter = new OpenmrsProfileExcludeFilter();
	
	/**
	 * @should create bean for openmrs 1_8 and later
	 * @should not create bean for openmrs 1_6 to 1_7
	 */
	@Override
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
		Map<String, Object> openmrsProfileAttributes = metadataReader.getAnnotationMetadata().getAnnotationAttributes(
		    "org.openmrs.annotation.OpenmrsProfile");
		if (openmrsProfileAttributes != null) {
			return openmrsProfileExcludeFilter.matchOpenmrsProfileAttributes(openmrsProfileAttributes);
		} else {
			return false;
		}
	}
	
}

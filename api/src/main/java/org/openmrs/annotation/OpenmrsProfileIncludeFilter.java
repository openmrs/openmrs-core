/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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

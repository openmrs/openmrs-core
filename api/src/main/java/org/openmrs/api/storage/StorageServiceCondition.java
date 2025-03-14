/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.storage;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Allows to conditionally enable storage service based on "storage_type" property.
 * <p>
 * It enables "local" storage by default.
 * 
 * @since 2.8.0, 2.7.4, 2.6.16, 2.5.15
 */
public class StorageServiceCondition implements Condition {
	private static final Logger log = LoggerFactory.getLogger(StorageServiceCondition.class);
	
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(Qualifier.class.getName());
		Object value = annotationAttributes != null ? annotationAttributes.get("value") : null;
		
		String storageType = context.getEnvironment().getProperty("storage_type", String.class, "local");
		if (value != null && storageType.equalsIgnoreCase(value.toString())) {
			log.info("Selected storage type: {}", storageType);
			return true;
		}
		return false;
	}
}

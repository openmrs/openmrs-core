/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openmrs.module.webservices.rest.web.resource.api.Resource;

/**
 * Indicates that the annotated class is a sub-resource of another Resource
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SubResource {
	
	/**
	 * @return the resource class that this sub-resource is a child of
	 */
	Class<? extends Resource> parent();
	
	/**
	 * @return the relative URI this sub-resource lives at (will be appended to the URI of the
	 *         parent resource)
	 */
	String path();
	
	Class<?> supportedClass();
	
	String[] supportedOpenmrsVersions();
	
	int order() default Integer.MAX_VALUE;
	
}

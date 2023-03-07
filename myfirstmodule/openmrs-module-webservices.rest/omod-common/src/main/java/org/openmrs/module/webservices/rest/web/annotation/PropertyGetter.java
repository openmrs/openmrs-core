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

import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;

/**
 * Use this annotation to mark a method in a {@link DelegatingCrudResource} implementation that
 * describes how to get a property on a delegate. (You would use this, for example, if you want to
 * expose a "attributes" property in the resource, but return from a different getter
 * (getActiveAttributes) from the delegate.) The "getter" method should have the form
 * "Object getXyz(T delegate)" and may be static.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyGetter {
	
	/**
	 * @return the name of the property the annotated method is a "getter" for.
	 */
	String value();
	
}

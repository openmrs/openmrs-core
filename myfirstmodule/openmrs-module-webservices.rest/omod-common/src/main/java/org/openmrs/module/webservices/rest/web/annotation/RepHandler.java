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

import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;

/**
 * Method-level annotation, which marks a method as being the "get" handler for a particular
 * representation. Methods marked with this annotation are only called if there is not a
 * getRepresentationDescription method, or it returns null for the given rep
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RepHandler {
	
	Class<? extends Representation> value() default NamedRepresentation.class;
	
	String name() default "";
	
}

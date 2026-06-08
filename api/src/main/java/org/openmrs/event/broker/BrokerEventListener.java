/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event.broker;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate methods that should listen to broker events.
 * 
 * @since 2.9.0
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@EventListener
public @interface BrokerEventListener {

	/**
	 * The identifier of the source this listener should listen to e.g. queue name.
	 * @return the source
	 */
	String value();

	/**
	 * The identifier of the broker or empty for default.
	 * @return the broker identifier
	 */
	String broker() default "";

	@AliasFor(annotation = EventListener.class, attribute = "condition")
	String condition() default "";
	
	@AliasFor(annotation = EventListener.class, attribute = "id")
	String id() default "";
}

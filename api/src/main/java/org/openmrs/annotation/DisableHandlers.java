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

import org.openmrs.api.handler.RequiredDataHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *    In {@link RequiredDataAdvice}, by default, RequiredDataHandlers are called on all child collections of the {@link OpenmrsObject} being handled.
 *    By annotating a Collection with a @DisableHandlers annotation, you specific that RequiredDataAdvice should NOT apply the specified
 *    handler(s) to a child collection.  For example:
 *
 *      private class ClassWithDisableHandlersAnnotation extends BaseOpenmrsData {
 *          @DisableHandlers(handlerTypes = {VoidHandler.class, SaveHandler.class})
 *          private List<Person> persons;
 *      }
 *
 *      You can disable all RequiredDataAdviceHandlers by specifying the parent class: @DisableHandlers(handlerTypes = { RequiredDataHandler.class })
 **/

@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DisableHandlers {
	
	/**
	 * The set of handlers to be be disabled
	 */
	public Class<? extends RequiredDataHandler>[] handlerTypes() default {};
	
}

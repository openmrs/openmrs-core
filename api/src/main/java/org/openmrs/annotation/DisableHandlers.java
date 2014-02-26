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

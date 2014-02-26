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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openmrs.api.handler.OpenmrsObjectSaveHandler;

/**
 * Annotation used to indicate that a method allows empty strings.
 * 
 * <pre>
 * &#064;AllowEmptyStrings
 * public void setName(String name);
 * </pre>
 * 
 * Note: This should be annotated on the setter methods.<br/>
 * <br/>
 * If this annotation is not present the the property will be set to null by the
 * {@link OpenmrsObjectSaveHandler} if the value is an empty string.  
 * 
 * @since 1.9
 * @see AllowLeadingOrTrailingWhitespace
 * @see OpenmrsObjectSaveHandler
 */
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AllowEmptyStrings {

}

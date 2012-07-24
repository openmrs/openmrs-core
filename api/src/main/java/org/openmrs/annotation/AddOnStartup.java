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

/**
 * Annotation used to describe constants in the PrivilegeConstants or RoleConstants class.
 * Constant`s description or/and it`s belonging to core constants can be marked with this
 * annotation. If you want constant to be put into a database at app startup, just mark it:
 * 
 * <pre>
 *      &#64;AddOnStartup(description = "Constant description")
 *      public static final String MANAGE_SMTH = "Manage smth";
 * </pre>
 * 
 * Or if you want to add only a constant`s description, mark it as "not core":
 * 
 * <pre>
 *      &#64;AddOnStartup(description = "Constant description", core = false)
 *      public static final String CONSTANT_SMTH = "Manage smth";
 * </pre>
 * 
 * @see org.openmrs.util.RoleConstants
 * @see org.openmrs.util.PrivilegeConstants
 * @see org.openmrs.util.OpenmrsConstants
 */
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AddOnStartup {
	
	public String description() default "";
	
	public boolean core() default true;
}

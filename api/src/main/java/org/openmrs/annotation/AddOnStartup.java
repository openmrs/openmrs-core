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

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
 * Annotation used to describe service layer authorization attributes.
 * <p>
 * For example, to require that the user have <i>either</i> View or Add privileges:
 * 
 * <pre>
 *     &#64;Authorized ({"View Users", "Add User"})
 *     public void getUsersByName(String name);
 * </pre>
 * or to require that they have all privileges
 * 
 * <pre>
 *     &#64;Authorized (value = {"Add Users", "Edit Users"}, requireAll=true)
 *     public void getUsersByName(String name);
 * </pre>
 * or to just require that they be authenticated:
 * 
 * <pre>
 *     &#64;Authorized ()
 *     public void getUsersByName(String name);
 * </pre>
 */
@Target( { ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Authorized {
	
	/**
	 * Returns the list of privileges needed to access a method. (i.e. "View Users"). Multiple
	 * privileges are compared with an "or" unless <code>requireAll</code> is set to true
	 * 
	 * @return String[] The secure method attributes
	 */
	public String[] value() default {};
	
	/**
	 * If set to true, will require that the user have <i>all</i> privileges listed in
	 * <code>value</code>. if false, user only has to have one of the privileges. Defaults to false
	 * 
	 * @return boolean true/false whether the privileges should be "and"ed together
	 */
	public boolean requireAll() default false;
	
}

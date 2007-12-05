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
 * Annotation used to describe service layer authorization attributes.
 * 
 * <p>For example, to require that the user have <i>either<i> View or Add
 * privileges:
 * <pre>
 *     &#64;Authorized ({"View Users", "Add User"})
 *     public void getUsersByName(String name);
 * </pre>
 * 
 *  or to require that they have all privileges
 * 
 * <pre>
 *     &#64;Authorized (value = {"Add Users", "Edit Users"}, requireAll=true)
 *     public void getUsersByName(String name);
 * </pre>
 * 
 * or to just require that they be authenticated:
 * 
 * <pre>
 *     &#64;Authorized ()
 *     public void getUsersByName(String name);
 * </pre>
 * 
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Authorized {

    /**
     * Returns the list of privileges needed to access a method. (i.e. "View Users").  Multiple
     * privileges are compared with an "or" unless <code>requireAll</code> is set to true
     * 
     * @return String[] The secure method attributes 
     */
    public String[] value() default {};
    
    /**
     * If set to true, will require that the user have <i>all</i> privileges listed
     * in <code>value</code>.  if false, user only has to have one of the privileges.
     * 
     * Defaults to false
     * 
     * @return boolean true/false whether the privileges should be "and"ed together 
     */
    public boolean requireAll() default false; 
    
}

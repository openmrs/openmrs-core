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
 * This annotation serves as a general mechanism for providing metadata about a class that serves as
 * a 'Handler' for another class or classes. <br/>
 * <br/>
 * This is generally useful for situations where a <em>Handler</em> class has a method that is only
 * valid for certain implementations of a given class, but for which it is not desirable to restrict
 * this method to a given type. <br/>
 * <br/>
 * As an example, take {@link org.springframework.validation.Validator} class and implementations.
 * As currently designed, a Validator takes in an open-ended Object to validate:
 * <code>public void validate(Object obj, Errors errors)</code> <br/>
 * <br/>
 * To provide more context around what type of objects may successfully be passed in here for
 * validation, a Validator also requires implementation of:
 * <code>public boolean supports(Class c)</code> <br/>
 * <br/>
 * The weakness of this approach is that the classes that this Validator supports are not exposed.
 * You must first have a class to Validate and check whether it is supported. It would be preferable
 * if each Validator exposed as class metadata the type of Objects that it was capable of
 * Validating. Then, via reflection, much more could be determined about the appropriate Validator
 * for a given object than is currently possible. This is the problem that this annotation aims to
 * solve. <br/>
 * <br/>
 * The use of this method would therefore be as follows:<br/>
 * 
 * <pre>
 * @Handler(supports={Order.class ) public class OrderValidator implements Validator { ... } 
 * </pre>
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Handler {
	
	public Class<?>[] supports() default {};
	
	/**
	 * Provides a means for specifying the relative order of this Handler against another Handler of
	 * the same type. For example, if two handlers are registered as capable of handling a
	 * {@link Date} class, and one handler had an order of 100 and the other had an order of 50, the
	 * consuming code could utilize this information to determine which handler is preferred. By
	 * convention, the handler with the lowest order generally gains precedence.
	 * 
	 * @return an int specifying the relative order of this Handler
	 */
	public int order() default Integer.MAX_VALUE;
	
}

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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.core.annotation.AnnotationUtils;

/**
 * Annotation attributes metadata implementation used for authorization method interception.
 * <p>
 * This <code>Attributes</code> implementation will return security configuration for classes
 * described using the <code>Secured</code> Java 5 annotation.
 * <p>
 * The <code>SecurityAnnotationAttributes</code> implementation can be used to configure a
 * <code>MethodDefinitionAttributes</code> and <code>MethodSecurityInterceptor</code> bean
 * definition (see below).
 * <p>
 * For example:
 * 
 * <pre>
 * &lt;bean id="attributes" 
 *     class="org.acegisecurity.annotation.SecurityAnnotationAttributes"/&gt;
 * 
 * &lt;bean id="objectDefinitionSource" 
 *     class="org.acegisecurity.intercept.method.MethodDefinitionAttributes"&gt;
 *     &lt;property name="attributes"&gt;
 *         &lt;ref local="attributes"/&gt;
 *     &lt;/property&gt;
 * &lt;/bean&gt;
 * 
 * &lt;bean id="securityInterceptor" 
 *     class="org.acegisecurity.intercept.method.aopalliance.MethodSecurityInterceptor"&gt;
 *      . . .
 *      &lt;property name="objectDefinitionSource"&gt;
 *          &lt;ref local="objectDefinitionSource"/&gt;
 *      &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * <p>
 * These security annotations are similar to the Commons Attributes approach, however they are
 * using Java 5 language-level metadata support.
 * 
 * @see org.openmrs.annotation.Authorized
 */
public class AuthorizedAnnotationAttributes {
	
	private static final String UNSUPPORTED_OPERATION = "Unsupported operation";
	
	/**
	 * Get the <code>Secured</code> attributes for a given target class.
	 * 
	 * @param target The target method
	 * @return Collection of <code>SecurityConfig</code>
	 */
	public Collection<String> getAttributes(Class<?> target) {
		Set<String> attributes = new HashSet<>();

		Authorized authorized = AnnotationUtils.findAnnotation(target, Authorized.class);
		if (authorized != null) {
			Collections.addAll(attributes, authorized.value());
		}
		
		return attributes;
	}
	
	/**
	 * Get the <code>Secured</code> attributes for a given target method.
	 * 
	 * @param method The target method
	 * @return Collection of <code>SecurityConfig</code>
	 */
	public Collection<String> getAttributes(Method method) {
		Set<String> attributes = new HashSet<>();

		Authorized authorized = AnnotationUtils.findAnnotation(method, Authorized.class);
		if (authorized != null) {
			Collections.addAll(attributes, authorized.value());
		}
		
		return attributes;
	}
	
	/**
	 * Returns whether or not to require that the user have all of the privileges in order to be
	 * "authorized" for this class
	 * 
	 * @param target the class to act on
	 * @return boolean true/false whether to "and" privileges together
	 * @see org.openmrs.annotation.Authorized#requireAll()
	 */
	public boolean getRequireAll(Class<?> target) {
		Authorized authorized = AnnotationUtils.findAnnotation(target, Authorized.class);
		if (authorized != null) {
			return authorized.requireAll();
		}
		return false;
	}
	
	/**
	 * Returns whether or not to require that the user have all of the privileges in order to be
	 * "authorized" for this method
	 * 
	 * @param method
	 * @return boolean true/false whether to "and" privileges together
	 * @see org.openmrs.annotation.Authorized#requireAll()
	 */
	public boolean getRequireAll(Method method) {
		Authorized authorized = AnnotationUtils.findAnnotation(method, Authorized.class);
		if (authorized != null) {
			return authorized.requireAll();
		}
		return false;
	}
	
	/**
	 * Determine if this method has the @Authorized annotation even on it
	 * 
	 * @param method
	 * @return boolean true/false whether this method is annotated for OpenMRS
	 */
	public boolean hasAuthorizedAnnotation(Method method) {
		Authorized authorized = AnnotationUtils.findAnnotation(method, Authorized.class);
	
		return (authorized != null);
	}
	
	public Collection<?> getAttributes(Class<?> clazz, Class<?> filter) {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
	}
	
	public Collection<?> getAttributes(Method method, Class<?> clazz) {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
	}
	
	public Collection<?> getAttributes(Field field) {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
	}
	
	public Collection<?> getAttributes(Field field, Class<?> clazz) {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
	}
	
}

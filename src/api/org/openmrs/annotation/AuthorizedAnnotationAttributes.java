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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.metadata.Attributes;

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
 *     class="org.acegisecurity.annotation.SecurityAnnotationAttributes"/>
 * 
 * &lt;bean id="objectDefinitionSource" 
 *     class="org.acegisecurity.intercept.method.MethodDefinitionAttributes">
 *     &lt;property name="attributes">
 *         &lt;ref local="attributes"/>
 *     &lt;/property>
 * &lt;/bean>
 * 
 * &lt;bean id="securityInterceptor" 
 *     class="org.acegisecurity.intercept.method.aopalliance.MethodSecurityInterceptor">
 *      . . .
 *      &lt;property name="objectDefinitionSource">
 *          &lt;ref local="objectDefinitionSource"/>
 *      &lt;/property>
 * &lt;/bean>
 * </pre>
 * <p>
 * These security annotations are similiar to the Commons Attributes approach, however they are
 * using Java 5 language-level metadata support.
 * 
 * @see org.openmrs.annotation.Authorized
 */
@SuppressWarnings("unchecked")
public class AuthorizedAnnotationAttributes implements Attributes {
	
	/**
	 * Get the <code>Secured</code> attributes for a given target class.
	 * 
	 * @param method The target method
	 * @return Collection of <code>SecurityConfig</code>
	 * @see Attributes#getAttributes
	 */
	public Collection getAttributes(Class target) {
		Set<String> attributes = new HashSet<String>();
		for (Annotation annotation : target.getAnnotations()) {
			// check for Secured annotations
			if (annotation instanceof Authorized) {
				Authorized attr = (Authorized) annotation;
				for (String privilege : attr.value()) {
					attributes.add(privilege);
				}
				break;
			}
		}
		return attributes;
	}
	
	/**
	 * Get the <code>Secured</code> attributes for a given target method.
	 * 
	 * @param method The target method
	 * @return Collection of <code>SecurityConfig</code>
	 * @see Attributes#getAttributes
	 */
	public Collection getAttributes(Method method) {
		Set<String> attributes = new HashSet<String>();
		
		for (Annotation annotation : method.getAnnotations()) {
			// check for Secured annotations
			if (annotation instanceof Authorized) {
				Authorized attr = (Authorized) annotation;
				for (String privilege : attr.value()) {
					attributes.add(privilege);
				}
				break;
			}
		}
		return attributes;
	}
	
	/**
	 * Returns whether or not to require that the user have all of the privileges in order to be
	 * "authorized" for this class
	 * 
	 * @param method
	 * @return boolean true/false whether to "and" privileges together
	 * @see org.openmrs.annotation.Authorized#requireAll()
	 */
	public boolean getRequireAll(Class target) {
		for (Annotation annotation : target.getAnnotations()) {
			// check for Secured annotations
			if (annotation instanceof Authorized) {
				Authorized attr = (Authorized) annotation;
				return attr.requireAll();
			}
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
		for (Annotation annotation : method.getAnnotations()) {
			// check for Secured annotations
			if (annotation instanceof Authorized) {
				Authorized attr = (Authorized) annotation;
				return attr.requireAll();
			}
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
		for (Annotation annotation : method.getAnnotations()) {
			// check for Secured annotations
			if (annotation instanceof Authorized) {
				return true;
			}
		}
		
		return false;
	}
	
	public Collection getAttributes(Class clazz, Class filter) {
		throw new UnsupportedOperationException("Unsupported operation");
	}
	
	public Collection getAttributes(Method method, Class clazz) {
		throw new UnsupportedOperationException("Unsupported operation");
	}
	
	public Collection getAttributes(Field field) {
		throw new UnsupportedOperationException("Unsupported operation");
	}
	
	public Collection getAttributes(Field field, Class clazz) {
		throw new UnsupportedOperationException("Unsupported operation");
	}
	
}

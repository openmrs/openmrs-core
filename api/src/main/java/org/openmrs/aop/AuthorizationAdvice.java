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
package org.openmrs.aop;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.annotation.AuthorizedAnnotationAttributes;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * This class provides the authorization AOP advice performed before every service layer method
 * call.
 */
public class AuthorizationAdvice implements MethodBeforeAdvice {
	
	/**
	 * Logger for this class and subclasses
	 */
	protected static final Log log = LogFactory.getLog(AuthorizationAdvice.class);
	
	/**
	 * Allows us to check whether a user is authorized to access a particular method.
	 * 
	 * @param method
	 * @param args
	 * @param target
	 * @throws Throwable
	 */
	@SuppressWarnings( { "unchecked" })
	public void before(Method method, Object[] args, Object target) throws Throwable {
		
		if (log.isDebugEnabled())
			log.debug("Calling authorization advice before " + method.getName());
		
		User user = Context.getAuthenticatedUser();
		
		if (log.isDebugEnabled()) {
			log.debug("User " + user);
			if (user != null)
				log.debug("has roles " + user.getAllRoles());
		}
		
		AuthorizedAnnotationAttributes attributes = new AuthorizedAnnotationAttributes();
		Collection<String> attrs = attributes.getAttributes(method);
		boolean requireAll = attributes.getRequireAll(method);
		
		// Only execute if the "secure" method has authorization attributes
		// Iterate through required privileges and return only if the user has
		// one of them
		if (attrs.size() > 0) {
			for (String privilege : attrs) {
				
				// skip null privileges
				if (privilege == null || privilege.length() < 1)
					return;
				
				if (log.isDebugEnabled())
					log.debug("User has privilege " + privilege + "? " + Context.hasPrivilege(privilege));
				
				if (Context.hasPrivilege(privilege)) {
					if (requireAll == false) {
						// if not all required, the first one that they have
						// causes them to "pass"
						return;
					}
				} else if (requireAll == true) {
					// if all are required, the first miss causes them
					// to "fail"
					throwUnauthorized(user, method, privilege);
				}
			}
			
			if (requireAll == false) {
				// If there's no match, then we know there are privileges and
				// that the user didn't have any of them. The user is not
				// authorized to access the method
				throwUnauthorized(user, method, attrs);
			}
			
		} else if (attributes.hasAuthorizedAnnotation(method)) {
			// if there are no privileges defined, just require that 
			// the user be authenticated
			if (Context.isAuthenticated() == false)
				throwUnauthorized(user, method);
		}
	}
	
	/**
	 * Throws an APIAuthorization exception stating why the user failed
	 * 
	 * @param user authenticated user
	 * @param method acting method
	 * @param attrs Collection of String privilege names that the user must have
	 */
	private void throwUnauthorized(User user, Method method, Collection<String> attrs) {
		if (log.isDebugEnabled())
			log.debug("User " + user + " is not authorized to access " + method.getName());
		throw new APIAuthenticationException("Privileges required: " + attrs);
	}
	
	/**
	 * Throws an APIAuthorization exception stating why the user failed
	 * 
	 * @param user authenticated user
	 * @param method acting method
	 * @param attrs privilege names that the user must have
	 */
	private void throwUnauthorized(User user, Method method, String attr) {
		if (log.isDebugEnabled())
			log.debug("User " + user + " is not authorized to access " + method.getName());
		throw new APIAuthenticationException("Privilege required: " + attr);
	}
	
	/**
	 * Throws an APIAuthorization exception stating why the user failed
	 * 
	 * @param user authenticated user
	 * @param method acting method
	 */
	private void throwUnauthorized(User user, Method method) {
		if (log.isDebugEnabled())
			log.debug("User " + user + " is not authorized to access " + method.getName());
		throw new APIAuthenticationException("Basic authentication required");
	}
}

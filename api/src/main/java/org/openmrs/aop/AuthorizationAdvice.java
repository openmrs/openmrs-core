/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.User;
import org.openmrs.annotation.AuthorizedAnnotationAttributes;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * This class provides the authorization AOP advice performed before every service layer method
 * call.
 */
public class AuthorizationAdvice implements MethodBeforeAdvice {
	
	/**
	 * Logger for this class and subclasses
	 */
	private static final Logger log = LoggerFactory.getLogger(AuthorizationAdvice.class);
        private static final String USER_IS_NOT_AUTHORIZED_TO_ACCESS = "User {} is not authorized to access {}";
	
	/**
	 * Allows us to check whether a user is authorized to access a particular method.
	 * 
	 * @param method
	 * @param args
	 * @param target
	 * @throws Throwable
	 * <strong>Should</strong> notify listeners about checked privileges
	 */
	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		
		log.debug("Calling authorization advice before {}", method.getName());
		
		if (log.isDebugEnabled()) {
			User user = Context.getAuthenticatedUser();
			log.debug("User {}", user);
			if (user != null) {
				log.debug("has roles {}", user.getAllRoles());
			}
		}
		
		AuthorizedAnnotationAttributes attributes = new AuthorizedAnnotationAttributes();
		Collection<String> privileges = attributes.getAttributes(method);
		boolean requireAll = attributes.getRequireAll(method);
		
		// Only execute if the "secure" method has authorization attributes
		// Iterate through required privileges and return only if the user has
		// one of them
		if (!privileges.isEmpty()) {
			for (String privilege : privileges) {
				
				// skip null privileges
				if (privilege == null || privilege.isEmpty()) {
					return;
				}
				
				log.debug("User has privilege {}? {}", privilege, Context.hasPrivilege(privilege));
				
				if (Context.hasPrivilege(privilege)) {
					if (!requireAll) {
						// if not all required, the first one that they have
						// causes them to "pass"
						return;
					}
				} else {
					if (requireAll) {
						// if all are required, the first miss causes them
						// to "fail"
						throwUnauthorized(Context.getAuthenticatedUser(), method, privilege);
					}
				}
			}
			
			if (!requireAll) {
				// If there's no match, then we know there are privileges and
				// that the user didn't have any of them. The user is not
				// authorized to access the method
				throwUnauthorized(Context.getAuthenticatedUser(), method, privileges);
			}
			
		} else if (attributes.hasAuthorizedAnnotation(method) && !Context.isAuthenticated()) {
			throwUnauthorized(Context.getAuthenticatedUser(), method);
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
		log.debug(USER_IS_NOT_AUTHORIZED_TO_ACCESS, user, method.getName());
		throw new APIAuthenticationException(Context.getMessageSourceService().getMessage("error.privilegesRequired",
		    new Object[] { StringUtils.join(attrs, ",") }, null));
	}
	
	/**
	 * Throws an APIAuthorization exception stating why the user failed
	 * 
	 * @param user authenticated user
	 * @param method acting method
	 * @param attrs privilege names that the user must have
	 */
	private void throwUnauthorized(User user, Method method, String attr) {
		log.debug(USER_IS_NOT_AUTHORIZED_TO_ACCESS, user, method.getName());
		throw new APIAuthenticationException(Context.getMessageSourceService().getMessage("error.privilegesRequired",
		    new Object[] { attr }, null));
	}
	
	/**
	 * Throws an APIAuthorization exception stating why the user failed
	 * 
	 * @param user authenticated user
	 * @param method acting method
	 */
	private void throwUnauthorized(User user, Method method) {
		log.debug(USER_IS_NOT_AUTHORIZED_TO_ACCESS, user, method.getName());
		throw new APIAuthenticationException(Context.getMessageSourceService().getMessage("error.aunthenticationRequired"));
	}
}

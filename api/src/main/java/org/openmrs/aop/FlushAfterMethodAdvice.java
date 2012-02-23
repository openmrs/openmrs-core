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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.api.context.Context;

/**
 * Performs a flush after methods with the configured prefixes.
 * <p>
 * Prefixes configured by default include: save, create, purge, delete, retire, unretire, void,
 * unvoid
 */
public class FlushAfterMethodAdvice implements MethodInterceptor {
	
	private List<String> methodPrefixes = new ArrayList<String>(Arrays.asList("save", "create", "purge", "delete", "retire",
	    "unretire", "void", "unvoid", "addPatientToCohort", "mergeDuplicateFields", "stopVisits", "changePassword"));
	
	private List<String> excludedMethodPrefixes = new ArrayList<String>();
	
	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object result = invocation.proceed();
		
		String methodName = invocation.getMethod().getName();
		
		for (String excludedMethodPrefix : excludedMethodPrefixes) {
			if (methodName.startsWith(excludedMethodPrefix)) {
				return result;
			}
		}
		
		for (String methodPrefix : methodPrefixes) {
			if (methodName.startsWith(methodPrefix)) {
				Context.flushSession();
				return result;
			}
		}
		
		/*
		//Ideally service methods that update db should be marked with readOnly="false" and all others readOnly="true".
		//It is not yet the case, thus this code is commented out.
		Transactional transactional = invocation.getMethod().getAnnotation(Transactional.class);
		if (transactional == null) {
			transactional = invocation.getMethod().getDeclaringClass().getAnnotation(Transactional.class);
		}
		if (transactional != null) {
			if (!transactional.readOnly()) {
				Context.flushSession();
			}
		}
		*/

		return result;
	}
	
	/**
	 * @return the methodPrefixes
	 */
	public List<String> getMethodPrefixes() {
		return methodPrefixes;
	}
	
	/**
	 * @param methodPrefixes the methodPrefixes to set
	 */
	public void setMethodPrefixes(List<String> methodPrefixes) {
		this.methodPrefixes = methodPrefixes;
	}
	
	/**
	 * @return the excludedMethodPrefixes
	 */
	public List<String> getExcludedMethodPrefixes() {
		return excludedMethodPrefixes;
	}
	
	/**
	 * @param excludedMethodPrefixes the excludedMethodPrefixes to set
	 */
	public void setExcludedMethodPrefixes(List<String> excludedMethodPrefixes) {
		this.excludedMethodPrefixes = excludedMethodPrefixes;
	}
	
}

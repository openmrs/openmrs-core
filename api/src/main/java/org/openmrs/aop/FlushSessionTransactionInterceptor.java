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

import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.api.context.Context;
import org.springframework.aop.support.AopUtils;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Wraps TransactionInterceptor to perform flushes after methods annotated with
 * <code>@Transactional(readOnly=false)</code>.
 * 
 * @see TransactionInterceptor
 */
public class FlushSessionTransactionInterceptor extends TransactionInterceptor {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object result = super.invoke(invocation);
		
		// Work out the target class: may be <code>null</code>.
		// The TransactionAttributeSource should be passed the target class
		// as well as the method, which may be from an interface.
		Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
		
		// If the transaction attribute is null, the method is non-transactional.
		final TransactionAttribute txAttr = getTransactionAttributeSource().getTransactionAttribute(invocation.getMethod(),
		    targetClass);
		
		if (txAttr != null && !txAttr.isReadOnly()) {
			//Context.flushSession() is transactional so we need to prevent recursion and stack-overflow.
			if (!Context.class.equals(targetClass) && !invocation.getMethod().getName().equals("flushSession")) {
				Context.flushSession();
			}
		}
		
		return result;
	}
	
}

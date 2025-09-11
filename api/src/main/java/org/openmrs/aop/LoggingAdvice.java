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
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.User;
import org.openmrs.annotation.Logging;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

/**
 * This class provides the log4j aop around advice for our service layer. This advice is placed on
 * all services and daos via the spring application context. See
 * /metadata/api/spring/applicationContext.xml
 */
@Component("loggingInterceptor")
public class LoggingAdvice implements MethodInterceptor {
	
	/**
	 * List of all method name prefixes that result in DEBUG-level log messages
	 */
	private static final String[] SETTER_METHOD_PREFIXES = { "save", "create", "update", "void", "unvoid", "retire",
	        "unretire", "delete", "purge" };

	/**
	 * Logger for this class. Uses the name "org.openmrs.api" so that it seems to fit into the
	 * log4j2.xml configuration
	 */
	private final Logger log = LoggerFactory.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
	
	/**
	 * This method prints out trace statements for getters and debug statements for everything else
	 * ("setters"). If debugging is turned on, execution time for each method is printed as well.
	 * This method is called for every method in the Class/Service that it is wrapped around. This
	 * method should be fairly quick and light.
	 *
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		Method method = invocation.getMethod();
		String name = method.getName();
		
		// decide what type of logging we're doing with the current method and the loglevel
		boolean isSetterTypeOfMethod = OpenmrsUtil.stringStartsWith(name, SETTER_METHOD_PREFIXES);
		boolean logGetter = !isSetterTypeOfMethod && log.isTraceEnabled();
		boolean logSetter = isSetterTypeOfMethod && log.isDebugEnabled();
		
		// used for the execution time calculations
		long startTime = System.currentTimeMillis();
		
		// check if this method has the logging annotation on it
		Logging loggingAnnotation = null;
		if (logGetter || logSetter) {
			loggingAnnotation = AnnotationUtils.findAnnotation(method, Logging.class);
			if (loggingAnnotation != null && loggingAnnotation.ignore()) {
				logGetter = false;
				logSetter = false;
			}
		}
		
		if (logGetter || logSetter) {
			StringBuilder output = new StringBuilder();
			output.append("In method ").append(method.getDeclaringClass().getSimpleName()).append(".").append(name);
			
			// print the argument values unless we're ignoring all
			if (loggingAnnotation == null || !loggingAnnotation.ignoreAllArgumentValues()) {
				
				int x;
				Class<?>[] types = method.getParameterTypes();
				Object[] values = invocation.getArguments();
				
				// change the annotation array of indexes to a list of indexes to ignore
				List<Integer> argsToIgnore = new ArrayList<>();
				if (loggingAnnotation != null && loggingAnnotation.ignoredArgumentIndexes().length > 0) {
					for (int argIndexToIgnore : loggingAnnotation.ignoredArgumentIndexes()) {
						argsToIgnore.add(argIndexToIgnore);
					}
				}
				
				// loop over and print out each argument value
				output.append(". Arguments: ");
				for (x = 0; x < types.length; x++) {
					output.append(types[x].getSimpleName()).append("=");
					
					// if there is an annotation to skip this, print out a bogus string.
					if (argsToIgnore.contains(x)) {
						output.append("<Arg value ignored>");
					} else {
						output.append(values[x]);
					}
					
					output.append(", ");
				}
				
			}
			
			// print the string as either trace or debug
			if (logGetter) {
				log.trace(output.toString());
			} else if (logSetter) {
				log.debug(output.toString());
			}
		}
		
		try {
			// do the actual method we're wrapped around
			return invocation.proceed();
		}
		catch (Exception e) {
			if (logGetter || logSetter) {
				String username;
				User user = Context.getAuthenticatedUser();
				if (user == null) {
					username = "Guest (Not logged in)";
				} else {
					username = user.getUsername();
					if (username == null || username.length() == 0) {
						username = user.getSystemId();
					}
				}
				log.debug(String.format(
				    "An error occurred while executing this method.%nCurrent user: %s%nError message: %s", username, e
				            .getMessage()), e);
			}
			throw e;
		}
		finally {
			if (logGetter || logSetter) {
				StringBuilder output = new StringBuilder();
				output.append("Exiting method ").append(name);
				
				// only append execution time info if we're in debug mode
				if (log.isDebugEnabled()) {
					output.append(". execution time: ").append(System.currentTimeMillis() - startTime).append(" ms");
				}
				
				// print the string as either trace or debug
				if (logGetter) {
					log.trace(output.toString());
				} else if (logSetter) {
					log.debug(output.toString());
				}
			}
		}
		
	}
}

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
import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.User;
import org.openmrs.annotation.Logging;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * This class provides the log4j aop around advice for our service layer. This advice is placed on
 * all services and daos via the spring application context. See
 * /metadata/api/spring/applicationContext.xml
 */
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

		// This advice wraps every method of every service, so the overwhelmingly common case is that
		// neither level is enabled and there is nothing to do. Bail out before touching the method
		// name, the clock or a try/finally frame, none of which can affect the outcome here.
		final boolean traceEnabled = log.isTraceEnabled();
		final boolean debugEnabled = log.isDebugEnabled();
		if (!traceEnabled && !debugEnabled) {
			return invocation.proceed();
		}

		Method method = invocation.getMethod();
		String name = method.getName();
		
		// decide what type of logging we're doing with the current method and the loglevel
		boolean isSetterTypeOfMethod = OpenmrsUtil.stringStartsWith(name, SETTER_METHOD_PREFIXES);
		boolean logGetter = !isSetterTypeOfMethod && traceEnabled;
		boolean logSetter = isSetterTypeOfMethod && debugEnabled;

		// e.g. a getter when only DEBUG is enabled: the enabled level does not cover this method
		if (!logGetter && !logSetter) {
			return invocation.proceed();
		}

		// check if this method has the logging annotation on it
		Logging loggingAnnotation = AnnotationUtils.findAnnotation(method, Logging.class);
		if (loggingAnnotation != null && loggingAnnotation.ignore()) {
			return invocation.proceed();
		}

		// used for the execution time calculations; nanoTime is monotonic, so unlike wall-clock time it
		// cannot report a negative or inflated duration when the system clock is adjusted mid-call
		long startTime = System.nanoTime();

		StringBuilder output = new StringBuilder();
		output.append("In method ").append(method.getDeclaringClass().getSimpleName()).append(".").append(name);

		// print the argument values unless we're ignoring all
		if (loggingAnnotation == null || !loggingAnnotation.ignoreAllArgumentValues()) {
			
			Class<?>[] types = method.getParameterTypes();
			Object[] values = invocation.getArguments();
			
			// flag the indexes named by the annotation so the loop below can test them directly; indexes
			// outside the parameter list are ignored rather than treated as an error
			boolean[] argsToIgnore = null;
			if (loggingAnnotation != null && loggingAnnotation.ignoredArgumentIndexes().length > 0) {
				argsToIgnore = new boolean[types.length];
				for (int argIndexToIgnore : loggingAnnotation.ignoredArgumentIndexes()) {
					if (argIndexToIgnore >= 0 && argIndexToIgnore < types.length) {
						argsToIgnore[argIndexToIgnore] = true;
					}
				}
			}
			
			// loop over and print out each argument value
			output.append(". Arguments: ");
			for (int x = 0; x < types.length; x++) {
				output.append(types[x].getSimpleName()).append("=");
				
				// if there is an annotation to skip this, print out a bogus string.
				if (argsToIgnore != null && argsToIgnore[x]) {
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
		} else {
			log.debug(output.toString());
		}
		
		try {
			// do the actual method we're wrapped around
			return invocation.proceed();
		} catch (Exception e) {
			String username;
			User user = null;
			try {
				user = Context.getAuthenticatedUser();
			} catch (APIException ignored) {
				// no user context established
			}
			
			if (user == null) {
				username = "Guest (Not logged in)";
			} else {
				username = user.getUsername();
				if (username == null || username.length() == 0) {
					username = user.getSystemId();
				}
			}
			log.debug("An error occurred while executing this method.\nCurrent user: {}\nError message: {}", username,
			    e.getMessage(), e);
			throw e;
		} finally {
			StringBuilder exitOutput = new StringBuilder();
			exitOutput.append("Exiting method ").append(name);
			
			// only append execution time info if we're in debug mode
			if (debugEnabled) {
				exitOutput.append(". execution time: ").append(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime))
				        .append(" ms");
			}
			
			// output the string as either trace or debug
			if (logGetter) {
				log.trace(exitOutput.toString());
			} else {
				log.debug(exitOutput.toString());
			}
		}
		
	}
}

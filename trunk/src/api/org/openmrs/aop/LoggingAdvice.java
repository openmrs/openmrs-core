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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Logging;

/**
 * This class provides the log4j aop around advice for our service layer.
 * 
 * This advice is placed on all services and daos via the spring 
 * application context.  See /metadata/api/spring/applicationContext.xml
 * 
 */
public class LoggingAdvice implements MethodInterceptor {

	/**
	 * Logger for this class.  Uses the name "org.openmrs.api" so that it seems
	 * to fit into the log4j.xml configuration
	 */
	protected static final Log log = LogFactory.getLog("org.openmrs.api");
	
	/**
	 * This method prints out debug statements for getters and info
	 * statements for everything else ("setters").
	 * 
	 * If debugging is turned on, execution time for each method is printed as well.
	 * 
	 * This method is called for every method in the Class/Service that it 
	 * is wrapped around.  This method should be fairly quick and light.
	 * 
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation invocation) throws Throwable {
	    
    	Method method = invocation.getMethod();
    	String name = method.getName();
    	
    	// decide what type of logging we're doing with the current method and loglevel
    	boolean isGetterTypeOfMethod = name.startsWith("get") || name.startsWith("find");
    	boolean logGetter = isGetterTypeOfMethod && log.isDebugEnabled();
    	boolean logSetter = !isGetterTypeOfMethod && log.isInfoEnabled();
    	
    	// used for the execution time calculations
    	long startTime = new Date().getTime();
    	
    	if (logGetter || logSetter) {
    		StringBuilder output = new StringBuilder();
    		output.append("In method ")
    		      .append(method.getDeclaringClass().getSimpleName())
    		      .append(".")
    		      .append(name);
    		
    		// check if this method has the logging annotation on it
    		Logging loggingAnnotation = method.getAnnotation(Logging.class);
    		
    		// print the argument values unless we're ignoring all
    		if (loggingAnnotation == null || 
    			loggingAnnotation.ignoreAllArgumentValues() == false) {
	    		
	    		int x;
	    		Class<?>[] types = method.getParameterTypes();
	    		Object[] values = invocation.getArguments();
	    		
	    		// change the annotation array of indexes to a list of indexes to ignore
	    		List<Integer> argsToIgnore = new ArrayList<Integer>();
	    		if (loggingAnnotation != null && loggingAnnotation.ignoredArgumentIndexes().length > 0) {
	    			for (int argIndexToIgnore : loggingAnnotation.ignoredArgumentIndexes())
	    				argsToIgnore.add(argIndexToIgnore);
	    		}
	    		
	    		// loop over and print out each argument value
	    		output.append(". Arguments: ");
	    		for (x = 0; x < types.length; x++) {
	    			output.append(types[x].getSimpleName()).append("=");
	    			
	    			// if there is an annotation to skip this, print out a bogus string.
	    			if (argsToIgnore.contains(x))
	    				output.append("<Arg value ignored>");
	    			else
	    				output.append(values[x]);
	    			
	    			output.append(", ");
	    		}
	    		
    		}
    		
    		// print the string as either debug or info
    		if (logGetter)
    			log.debug(output.toString());
    		else if (logSetter)
    			log.info(output.toString());
    	}
    	
    	try {
    		// do the actual method we're wrapped around
    		return invocation.proceed();
    	}
    	catch (Throwable t) {
    		if (logGetter || logSetter)
    			log.error("An error occurred while executing this method. Error message: " + t.getMessage());
    		throw t;
    	}
    	finally {
    		if (logGetter || logSetter) {
        		StringBuilder output = new StringBuilder();
        		output.append("Exiting method ").append(name);
        		
        		// only append execution time info if we're in debug mode
        		if (log.isDebugEnabled())
        			output.append(". execution time: " + (new Date().getTime() - startTime)).append(" ms");
        		
        		// print the string as either debug or info
        		if (logGetter)
        			log.debug(output.toString());
        		else if (logSetter)
        			log.info(output.toString());
        	}
    	}

    }

}
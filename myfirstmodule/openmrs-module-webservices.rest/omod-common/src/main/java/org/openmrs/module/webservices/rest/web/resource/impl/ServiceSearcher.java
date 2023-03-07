/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.impl;

import java.lang.reflect.Method;
import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;

/**
 * Helper for {@link Searchable} implementations, which delegates to named service methods that do
 * paged searching (by String, Integer, Integer) and the matching count of that search.
 * 
 * @param <T> the generic type of the {@link List} that will be returned by the search method
 */
public class ServiceSearcher<T> {
	
	private Class<? extends OpenmrsService> serviceClass;
	
	private String searchMethod;
	
	private String countMethod;
	
	public ServiceSearcher(Class<? extends OpenmrsService> serviceClass, String searchMethod, String countMethod) {
		this.serviceClass = serviceClass;
		this.searchMethod = searchMethod;
		this.countMethod = countMethod;
	}
	
	/**
	 * Makes service calls to get the count and search results for the given query, and packages
	 * those up as an AlreadyPaged search result
	 * 
	 * @param query
	 * @param context
	 * @return
	 */
	public AlreadyPaged<T> search(String query, RequestContext context) {
		OpenmrsService service = Context.getService(serviceClass);
		List<T> results;
		Integer count;
		results = doPagedSearch(service, query, context);
		count = doCount(service, query, context);
		boolean hasMore = count > context.getStartIndex() + context.getLimit();
		return new AlreadyPaged<T>(context, results, hasMore, Long.valueOf(count));
	}
	
	/**
	 * Finds and invokes a search method whose name is given by searchMethod and whose signature
	 * includes one String, two Integers, and any number of boolean or Booleans
	 * 
	 * @param service
	 * @param query
	 * @param context
	 * @return
	 */
	private List<T> doPagedSearch(OpenmrsService service, String query, RequestContext context) {
		try {
			for (Method candidate : serviceClass.getMethods()) {
				if (candidate.getName().equals(searchMethod) && hasRightParameterTypes(candidate, 1, 2))
					return invokePagedSearchMethod(service, candidate, query, context);
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(searchMethod + " failed", ex);
		}
		throw new RuntimeException("Cannot find suitable method");
	}
	
	/**
	 * Finds and invokes a count method whose name is given by countMethod and whose signature
	 * includes one String, and any number of boolean or Booleans
	 * 
	 * @param service
	 * @param query
	 * @param context
	 * @return
	 */
	private int doCount(OpenmrsService service, String query, RequestContext context) {
		try {
			for (Method candidate : serviceClass.getMethods()) {
				if (candidate.getName().equals(countMethod) && hasRightParameterTypes(candidate, 1, 0)) {
					return invokeCountMethod(service, candidate, query, context);
				}
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(countMethod + " failed", ex);
		}
		throw new RuntimeException("Cannot find suitable method");
	}
	
	/**
	 * Tests whether the method has the expected number of String and Integer arguments
	 * 
	 * @param method
	 * @param expectedStrings
	 * @param expectedIntegers
	 * @return
	 */
	private boolean hasRightParameterTypes(Method method, int expectedStrings, int expectedIntegers) {
		int strings = 0;
		int integers = 0;
		for (Class<?> clazz : method.getParameterTypes()) {
			if (clazz.equals(String.class))
				++strings;
			else if (clazz.equals(Integer.class))
				++integers;
			else if (!clazz.equals(boolean.class) && !clazz.equals(Boolean.class))
				return false;
		}
		return strings == expectedStrings && integers == expectedIntegers;
	}
	
	/**
	 * Invokes a paged search method, using query as its String argument, and the context's
	 * startIndex and limit to the first two Integer arguments
	 * 
	 * @param service
	 * @param method
	 * @param query
	 * @param context
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<T> invokePagedSearchMethod(OpenmrsService service, Method method, String query, RequestContext context)
	        throws Exception {
		Object[] args = new Object[method.getParameterTypes().length];
		boolean firstInteger = true;
		for (int i = 0; i < method.getParameterTypes().length; ++i) {
			Class<?> clazz = method.getParameterTypes()[i];
			if (clazz.equals(String.class)) {
				args[i] = query;
			} else if (clazz.equals(Integer.class)) {
				if (firstInteger) {
					args[i] = context.getStartIndex();
					firstInteger = false;
				} else {
					args[i] = context.getLimit();
				}
			} else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
				args[i] = context.getIncludeAll();
			} else {
				throw new RuntimeException("Method has argument types that are not allowed");
			}
		}
		return (List<T>) method.invoke(service, args);
	}
	
	/**
	 * Invokes a count method, using query as its String argument
	 * 
	 * @param service
	 * @param method
	 * @param query
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private int invokeCountMethod(OpenmrsService service, Method method, String query, RequestContext context)
	        throws Exception {
		Object[] args = new Object[method.getParameterTypes().length];
		for (int i = 0; i < method.getParameterTypes().length; ++i) {
			Class<?> clazz = method.getParameterTypes()[i];
			if (clazz.equals(String.class)) {
				args[i] = query;
			} else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
				args[i] = context.getIncludeAll();
			} else {
				throw new RuntimeException("Method has argument types that are not allowed");
			}
		}
		return (Integer) method.invoke(service, args);
	}
}

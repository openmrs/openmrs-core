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
package org.openmrs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

/**
 * Utility class that provides useful methods for working with classes that are annotated with the
 * {@link Handler} annotation
 * 
 * @since 1.5
 */
public class HandlerUtil {
	
	private static Log log = LogFactory.getLog(HandlerUtil.class);
	
	/**
	 * Retrieves a List of all registered components from the Context that are of the passed
	 * handlerType and one or more of the following is true:
	 * <ul>
	 * <li>The handlerType is annotated as a {@link Handler} that supports the passed type</li>
	 * <li>The passed type is null - this effectively returns all components of the passed
	 * handlerType</li>
	 * </ul>
	 * The returned handlers are ordered in the list based upon the order property.
	 * 
	 * @param handlerType Indicates the type of class to return
	 * @param type Indicates the type that the given handlerType must support (or null for any)
	 * @return a List of all matching Handlers for the given parameters, ordered by Handler#order
	 * @should return a list of all classes that can handle the passed type
	 * @should return classes registered in a module
	 * @should return an empty list if no classes can handle the passed type
	 */
	public static <H, T> List<H> getHandlersForType(Class<H> handlerType, Class<T> type) {
		
		List<H> handlers = new ArrayList<H>();
		
		// First get all registered components of the passed class
		log.debug("Getting handlers of type " + handlerType + (type == null ? "" : " for class " + type.getName()));
		for (H handler : Context.getRegisteredComponents(handlerType)) {
			Handler handlerAnnotation = handler.getClass().getAnnotation(Handler.class);
			// Only consider those that have been annotated as Handlers
			if (handlerAnnotation != null) {
				// If no type is passed in return all handlers
				if (type == null) {
					log.debug("Found handler " + handler.getClass());
					handlers.add(handler);
				}
				// Otherwise, return all handlers that support the passed type
				else {
					for (int i = 0; i < handlerAnnotation.supports().length; i++) {
						Class<?> clazz = handlerAnnotation.supports()[i];
						if (clazz.isAssignableFrom(type)) {
							log.debug("Found handler: " + handler.getClass());
							handlers.add(handler);
						}
					}
				}
			}
		}
		
		// Return the list of handlers based on the order specified in the Handler annotation
		Collections.sort(handlers, new Comparator<H>() {
			
			public int compare(H o1, H o2) {
				return getOrderOfHandler(o1.getClass()).compareTo(getOrderOfHandler(o2.getClass()));
			}
		});
		
		return handlers;
	}
	
	/**
	 * Retrieves the preferred Handler for a given handlerType and type. A <em>preferred</em>
	 * handler is the Handler that has the lowest defined <em>order</em> attribute in it's
	 * annotation. If multiple Handlers are found for the passed parameters at the lowest specified
	 * order, then an APIException is thrown.
	 * 
	 * @param handlerType the class that is an annotated {@link Handler} to retrieve
	 * @param type the class that the annotated {@link Handler} must support
	 * @return the class of the passed hanlerType with the lowest configured order
	 * @should return the preferred handler for the passed handlerType and type
	 * @should throw a APIException if no handler is found
	 * @should throw a APIException if multiple preferred handlers are found
	 */
	public static <H, T> H getPreferredHandler(Class<H> handlerType, Class<T> type) {
		
		if (handlerType == null || type == null) {
			throw new IllegalArgumentException("You must specify both a handlerType and a type");
		}
		List<H> handlers = getHandlersForType(handlerType, type);
		if (handlers == null || handlers.isEmpty()) {
			throw new APIException("No " + handlerType + " is found that is able to handle a " + type);
		}
		
		if (handlers.size() > 1) {
			int order1 = getOrderOfHandler(handlers.get(0).getClass());
			int order2 = getOrderOfHandler(handlers.get(1).getClass());
			if (order1 == order2) {
				throw new APIException("No preferred handler specified of type " + handlerType + " for " + type);
			}
		}
		
		return handlers.get(0);
	}
	
	/**
	 * Utility method to return the order attribute of the {@link Handler} annotation on the passed
	 * class. If the passed class does not have a {@link Handler} annotation, a RuntimeException is
	 * thrown
	 * 
	 * @param handlerClass
	 * @return the order attribute value
	 */
	public static Integer getOrderOfHandler(Class<?> handlerClass) {
		Handler annotation = handlerClass.getAnnotation(Handler.class);
		if (annotation == null) {
			throw new APIException("Class " + handlerClass + " is not annotated as a Handler.");
		}
		return annotation.order();
	}
}

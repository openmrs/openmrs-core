/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Utility class that provides useful methods for working with classes that are annotated with the
 * {@link Handler} annotation
 * 
 * @since 1.5
 */
@Component
public class HandlerUtil implements ApplicationListener<ContextRefreshedEvent> {
	
	private static final Logger log = LoggerFactory.getLogger(HandlerUtil.class);
	
	private static volatile Map<Key, List<?>> cachedHandlers = new WeakHashMap<>();
	
	private static class Key {
		
		public final Class<?> handlerType;
		
		public final Class<?> type;
		
		public Key(Class<?> handlerType, Class<?> type) {
			this.handlerType = handlerType;
			this.type = type;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((handlerType == null) ? 0 : handlerType.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Key other = (Key) obj;
			if (handlerType == null) {
				if (other.handlerType != null) {
					return false;
				}
			} else if (!handlerType.equals(other.handlerType)) {
				return false;
			}
			if (type == null) {
				return other.type == null;
			} else {
				return type.equals(other.type);
			}
		}
		
	}
	
	public static void clearCachedHandlers() {
		cachedHandlers = new WeakHashMap<>();
	}
	
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
	 * <strong>Should</strong> return a list of all classes that can handle the passed type
	 * <strong>Should</strong> return classes registered in a module
	 * <strong>Should</strong> return an empty list if no classes can handle the passed type
	 */
	public static <H, T> List<H> getHandlersForType(Class<H> handlerType, Class<T> type) {
		List<?> list = cachedHandlers.get(new Key(handlerType, type));
		if (list != null) {
			return (List<H>) list;
		}
		
		List<H> handlers = new ArrayList<>();
		
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
		handlers.sort(Comparator.comparing(o -> getOrderOfHandler(o.getClass())));
		
		Map<Key, List<?>> newCachedHandlers = new WeakHashMap<>(cachedHandlers);
		newCachedHandlers.put(new Key(handlerType, type), handlers);
		cachedHandlers = newCachedHandlers;
		
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
	 * @return the class of the passed handlerType with the lowest configured order
	 * <strong>Should</strong> return the preferred handler for the passed handlerType and type
	 * <strong>Should</strong> throw a APIException if no handler is found
	 * <strong>Should</strong> throw a APIException if multiple preferred handlers are found
	 * <strong>Should</strong> should return patient validator for patient
	 * <strong>Should</strong> should return person validator for person
	 */
	public static <H, T> H getPreferredHandler(Class<H> handlerType, Class<T> type) {
		
		if (handlerType == null || type == null) {
			throw new IllegalArgumentException("You must specify both a handlerType and a type");
		}
		List<H> handlers = getHandlersForType(handlerType, type);
		if (handlers == null || handlers.isEmpty()) {
			throw new APIException("handler.type.not.found", new Object[] { handlerType, type });
		}
		
		if (handlers.size() > 1) {
			int order1 = getOrderOfHandler(handlers.get(0).getClass());
			int order2 = getOrderOfHandler(handlers.get(1).getClass());
			if (order1 == order2) {
				throw new APIException("handler.type.multiple", new Object[] { handlerType, type });
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
			throw new APIException("class.not.annotated.as.handler", new Object[] { handlerClass });
		}
		return annotation.order();
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		clearCachedHandlers();
	}
}

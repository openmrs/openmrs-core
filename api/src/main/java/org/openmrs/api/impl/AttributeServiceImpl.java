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
package org.openmrs.api.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.api.APIException;
import org.openmrs.api.AttributeService;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.handler.AttributeHandler;
import org.openmrs.attribute.handler.StringAttributeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Core implementation of {@link AttributeService}
 * @since 1.9
 */
public class AttributeServiceImpl extends BaseOpenmrsService implements AttributeService {
	
	@Autowired
	private List<AttributeHandler<?>> allRegisteredHandlers;
	
	private transient Map<String, Class<? extends AttributeHandler<?>>> prioritizedHandlerClasses;
	
	/**
	 * @see org.openmrs.api.AttributeService#getLogicalTypes()
	 */
	@Override
	public Set<String> getLogicalTypes() {
		if (prioritizedHandlerClasses == null) {
			prioritizeHandlers();
		}
		return Collections.unmodifiableSet(prioritizedHandlerClasses.keySet());
	}
	
	/**
	 * @see org.openmrs.api.AttributeService#getHandler(java.lang.String, java.lang.String)
	 */
	@Override
	public AttributeHandler<?> getHandler(String logicalType, String handlerConfig) {
		if (prioritizedHandlerClasses == null) {
			prioritizeHandlers();
		}
		Class<? extends AttributeHandler<?>> handlerClass = prioritizedHandlerClasses.get(logicalType);
		if (handlerClass == null)
			return newDefaultHandler();
		AttributeHandler<?> handler;
		try {
			handler = handlerClass.newInstance();
			if (handlerConfig != null)
				handler.setConfiguration(handlerConfig);
			return handler;
		}
		catch (Exception ex) {
			throw new APIException("Error instantiating handler", ex);
		}
	}
	
	/**
	 * @return a newly-instantiated default handler, for use when no suitable handler is found, but we need
	 * to do _something_
	 */
	private AttributeHandler<?> newDefaultHandler() {
		return new StringAttributeHandler();
	}
	
	/**
	 * @see org.openmrs.api.AttributeService#getHandler(org.openmrs.attribute.AttributeType)
	 */
	@Override
	public AttributeHandler<?> getHandler(AttributeType<?> attributeType) {
		return Context.getAttributeService().getHandler(attributeType.getLogicalType(), attributeType.getHandlerConfig());
	}
	
	/**
	 * private method that prioritizes all registered handlers so we can quickly determin which to use for
	 * each logical type
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" })
	private synchronized void prioritizeHandlers() {
		if (prioritizedHandlerClasses == null) {
			prioritizedHandlerClasses = new HashMap<String, Class<? extends AttributeHandler<?>>>();
			for (AttributeHandler<?> handler : allRegisteredHandlers) {
				Class<? extends AttributeHandler> clazz = handler.getClass();
				if (!prioritizedHandlerClasses.containsKey(handler.getLogicalTypeHandled())) {
					prioritizedHandlerClasses.put(handler.getLogicalTypeHandled(),
					    (Class<? extends AttributeHandler<?>>) clazz);
				} else {
					int candidateOrder = getOrder((Class<? extends AttributeHandler<?>>) clazz);
					int existingOrder = getOrder(prioritizedHandlerClasses.get(handler.getLogicalTypeHandled()));
					if (candidateOrder < existingOrder)
						prioritizedHandlerClasses.put(handler.getLogicalTypeHandled(),
						    (Class<? extends AttributeHandler<?>>) clazz);
				}
			}
		}
	}
	
	/**
	 * @return the value of any {@link Order} annotation if defined, otherwise defaults to the lowest
	 * precedence
	 */
	private int getOrder(Class<?> clazz) {
		int order = Ordered.LOWEST_PRECEDENCE;
		Order orderAnnotation = clazz.getAnnotation(Order.class);
		if (orderAnnotation != null)
			order = orderAnnotation.value();
		return order;
	}
	
}

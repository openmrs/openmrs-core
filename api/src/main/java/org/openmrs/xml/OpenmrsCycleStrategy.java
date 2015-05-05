/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.xml;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsConstants;
import org.simpleframework.xml.graph.CycleStrategy;
import org.simpleframework.xml.stream.NodeMap;

/**
 * This is a specific serialization strategy developed so that the OpenMRS objects that happen to be
 * proxied by Hibernate will be serialized correctly.<br/>
 * Use:
 *
 * <pre>
 * 		Serializer serializer = new Persister(new OpenmrsCycleStrategy());
 * 		serializer.write(someObject, outputStream);
 * </pre>
 *
 * @deprecated - Use OpenmrsSerializer from Context.getSerializationService.getDefaultSerializer()
 */
@Deprecated
public class OpenmrsCycleStrategy extends CycleStrategy {
	
	private static final Log log = LogFactory.getLog(OpenmrsCycleStrategy.class);
	
	/**
	 * If true, the serialization will be kept to a minimum as decided in the methods marked with @Replace
	 * annotations
	 */
	private boolean shortSerialization = false;
	
	/**
	 * Overriding the default constructor so we can set our label logic onto the strategy. This
	 * label logic will convert hibernate proxy class names to their equivalent pojo class names
	 */
	public OpenmrsCycleStrategy() {
		super();
		setLabelLogic(new OpenmrsLabelLogic());
		setReferenceLogic(new OpenmrsReferenceLogic());
	}
	
	/**
	 * Custom constructor to set whether this serialization will be a short one or not. The
	 *
	 * @param isShortSerialization
	 */
	public OpenmrsCycleStrategy(boolean isShortSerialization) {
		this();
		this.shortSerialization = isShortSerialization;
	}
	
	/**
	 * @see org.simpleframework.xml.graph.CycleStrategy#setRoot(java.lang.Class, java.lang.Object,
	 *      org.simpleframework.xml.stream.NodeMap, java.util.Map)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean setRoot(Class field, Object value, NodeMap node, Map map) {
		
		// if the constructor was called to mark this as a short serialization,
		// put that property into the session map so that the @Replace methods
		// have access and know about it.
		if (shortSerialization) {
			map.put(OpenmrsConstants.SHORT_SERIALIZATION, Boolean.TRUE);
		}
		
		log.debug("Setting root as class: " + field);
		
		// continue as normal
		return super.setRoot(field, value, node, map);
	}
	
}

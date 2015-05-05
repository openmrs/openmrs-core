/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds all OpenMRS event listeners
 */
public class EventListeners {
	
	private static Log log = LogFactory.getLog(EventListeners.class);
	
	private static List<GlobalPropertyListener> globalPropertyListeners = null;
	
	public EventListeners() {
	}
	
	public List<GlobalPropertyListener> getGlobalPropertyListeners() {
		return globalPropertyListeners;
	}
	
	/**
	 * This setter acts more like an "appender".  If the list already has elements, calling this method
	 * will <b>add to</b> the list of listeners instead of replacing it.
	 *
	 * @param globalPropertyListeners
	 */
	public void setGlobalPropertyListeners(List<GlobalPropertyListener> globalPropertyListeners) {
		if (log.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer();
			for (GlobalPropertyListener gpl : globalPropertyListeners) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(gpl.getClass().getName());
			}
			log.debug("GlobalPropertyListeners set to: " + sb.toString());
			
		}
		
		if (EventListeners.globalPropertyListeners == null) {
			EventListeners.globalPropertyListeners = globalPropertyListeners;
		} else {
			for (GlobalPropertyListener gpl : globalPropertyListeners) {
				if (!EventListeners.globalPropertyListeners.contains(gpl)) {
					EventListeners.globalPropertyListeners.add(gpl);
				}
			}
		}
	}
	
	/**
	 * Convenience method called by spring to reset the static list of event
	 * listeners.<br/>
	 * Without this, the event listener list continues to grow with every Spring
	 * restart. (and is a memory leak)
	 *
	 * @param nullList
	 *            if true, nulls the list instead of just clearing it
	 *
	 * @see applicationContext-service.xml
	 */
	public void setGlobalPropertyListenersToEmpty(boolean nullList) {
		if (nullList) {
			setGlobalPropertyListeners(null);
		} else if (EventListeners.globalPropertyListeners != null) {
			EventListeners.globalPropertyListeners.clear();
		}
	}
	
}

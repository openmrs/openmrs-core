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
				if (sb.length() > 0)
					sb.append(", ");
				sb.append(gpl.getClass().getName());
			}
			log.debug("GlobalPropertyListeners set to: " + sb.toString());
			
		}
		
		if (EventListeners.globalPropertyListeners == null)
			EventListeners.globalPropertyListeners = globalPropertyListeners;
		else {
			for (GlobalPropertyListener gpl : globalPropertyListeners) {
				if (!EventListeners.globalPropertyListeners.contains(gpl))
					EventListeners.globalPropertyListeners.add(gpl);
			}
		}
	}
	
}

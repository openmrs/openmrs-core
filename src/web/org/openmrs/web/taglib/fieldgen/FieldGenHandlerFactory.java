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
package org.openmrs.web.taglib.fieldgen;

import java.util.Map;

/**
 * This factory stores and returns the fieldgen handlers
 * 
 * These variables are set in the openmrs-servlet.xml and
 * are populated via spring injection
 * 
 * @see FieldGenHandler
 */
public class FieldGenHandlerFactory {
	
	private Map<String,String> handlers = null;
	
	private static FieldGenHandlerFactory singleton;
	
	/**
	 * Generic constructor
	 */
	public FieldGenHandlerFactory() {
		singleton = this;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public static FieldGenHandlerFactory getSingletonInstance() {
		if (singleton == null)
			throw new RuntimeException("Not Yet Instantiated");
		else
			return singleton;
	}

	/**
	 * @return Returns the handlers.
	 */
	public Map<String,String> getHandlers() {
		return handlers;
	}

	/**
	 * Appends the given handlers to the current map of handlers
	 * 
	 * @param handlers The handlers to set.
	 */
	public void setHandlers(Map<String,String> handlers) {
		if (this.handlers == null)
			this.handlers = handlers;
		else
			this.handlers.putAll(handlers);
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param className
	 * @return
	 */
	public String getHandlerByClassName(String className) {
		if ( className != null ) {
			if ( handlers.containsKey(className) ) {
				return handlers.get(className);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}

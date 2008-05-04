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

public class FieldGenHandlerFactory {
	
	private Map<String,String> handlers;
	
	private static FieldGenHandlerFactory singleton;
	
	public FieldGenHandlerFactory() {
		singleton = this;
	}
	
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
	 * @param handlers The handlers to set.
	 */
	public void setHandlers(Map<String,String> handlers) {
		this.handlers = handlers;
	}
	
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

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib.fieldgen;

import java.util.Map;

/**
 * This factory stores and returns the fieldgen handlers These variables are set in the
 * openmrs-servlet.xml and are populated via spring injection
 *
 * @see FieldGenHandler
 */
public class FieldGenHandlerFactory {
	
	private Map<String, String> handlers = null;
	
	private static FieldGenHandlerFactory singleton;
	
	/**
	 * Generic constructor
	 */
	public FieldGenHandlerFactory() {
		if (singleton == null) {
			singleton = this;
		}
	}
	
	/**
	 * Auto generated method comment
	 *
	 * @return
	 */
	public static FieldGenHandlerFactory getSingletonInstance() {
		if (singleton == null) {
			throw new RuntimeException("Not Yet Instantiated");
		} else {
			return singleton;
		}
	}
	
	/**
	 * @return Returns the handlers.
	 */
	public Map<String, String> getHandlers() {
		return singleton.handlers;
	}
	
	/**
	 * Appends the given handlers to the current map of handlers
	 *
	 * @param handlers The handlers to set.
	 */
	public void setHandlers(Map<String, String> handlers) {
		if (singleton.handlers == null) {
			singleton.handlers = handlers;
		} else {
			singleton.handlers.putAll(handlers);
		}
	}
	
	/**
	 * Auto generated method comment
	 *
	 * @param className
	 * @return
	 */
	public String getHandlerByClassName(String className) {
		if (className != null) {
			if (singleton.handlers.containsKey(className)) {
				return singleton.handlers.get(className);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}

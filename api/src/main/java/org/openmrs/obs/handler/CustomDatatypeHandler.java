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

package org.openmrs.obs.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract handler class for convenience methods to be used when specifically storing Domain
 * Objects as answers for Complex Observations.
 */
public class CustomDatatypeHandler {
	
	public static final Log log = LogFactory.getLog(CustomDatatypeHandler.class);
	
	/**
	 * The default Constructor method
	 */
	public CustomDatatypeHandler() {
		
	}
	
	/**
	 * Gets the display link.
	 * 
	 * @return the display link
	 */
	public String getDisplayLink() {
		return null;
	}
	
}

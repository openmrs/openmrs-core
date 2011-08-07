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

package org.openmrs.web.obs.handler;

import org.openmrs.obs.handler.LocationHandler;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

/**
 * Handler for storing Location objects as the value for Complex Observations. 
 * The Location Id of each Location object is stored in the value_complex column of the 
 * Obs table. This value may be preceded by an optional displayable value. 
 * 
 * This class extends org.openmrs.obs.handler.LocationHandler
 * for LocationHandlers. You may also consider extending
 * Locationhandler class to meet your requirements. 
 * 
 * There may be several classes which extend
 * LocationHandler. Out of these, only one will be loaded by Spring. The class to be loaded will be
 * decided based on the @Order annotation value. 
 * 
 * As default, LocationHandler will have the lowest possible
 * priority, and will be overridden by LocationFieldGenObsHandler, which lets us use fieldGen tags
 * for data entry.
 */
@Component
@Order(0)
public class LocationFieldGenObsHandler extends LocationHandler implements FieldGenObsHandler {
	
	/**
	 * Method returns widgetName to be used by fieldGen tags
	 */
	@Override
	public String getWidgetName() {
		return "org.openmrs.Location";
	}
}

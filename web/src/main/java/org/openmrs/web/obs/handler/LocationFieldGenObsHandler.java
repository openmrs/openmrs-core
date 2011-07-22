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

@Component
@Order(0)
public class LocationFieldGenObsHandler extends LocationHandler implements FieldGenObsHandler {
	
	/* (non-Javadoc)
	 * @see org.openmrs.web.obs.handler.FieldGenObsHandler#getWidgetName()
	 */
	@Override
	public String getWidgetName() {
		return "org.openmrs.Location";
	}
}

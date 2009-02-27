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

import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

/**
 * This is the controller for the /web/web-inf/tags/locationField.tag.
 */
public class LocationHandler extends AbstractFieldGenHandler implements FieldGenHandler {
	
	private String defaultUrl = "location.field";
	
	/**
	 * @see org.openmrs.web.taglib.fieldgen.FieldGenHandler#run()
	 */
	public void run() {
		setUrl(defaultUrl);
		
		if (fieldGenTag != null) {
			String initialValue = "";
			checkEmptyVal((Location) null);
			Location l = (Location) this.fieldGenTag.getVal(); // get the initial value
			if (l != null) {
				if (l.getLocationId() != null)
					initialValue = l.getLocationId().toString();
			} else if (fieldGenTag.getAllowUserDefault()) {
				// if there is no default value and the tag at this point wants
				// to allow the user's chosen default value, set it here.
				// (an example of when the dev doesn't want a default value is if location
				//  is set to null by a previous user and the current user is only editing.  Therefore,
				//  the FieldGenTag.java#setAllowUserDefault() should only be set to true if
				//  creating an object for the first time)
				String userDefaultLocation = Context.getAuthenticatedUser().getUserProperty(
				    OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
				initialValue = userDefaultLocation;
			}
			
			String optionHeader = "";
			if (this.fieldGenTag.getParameterMap() != null) {
				optionHeader = (String) this.fieldGenTag.getParameterMap().get("optionHeader");
			}
			if (optionHeader == null)
				optionHeader = "";
			
			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
		}
	}
}

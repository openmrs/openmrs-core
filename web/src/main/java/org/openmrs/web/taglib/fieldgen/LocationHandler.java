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
				if (l.getLocationId() != null) {
					initialValue = l.getLocationId().toString();
				}
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
			if (optionHeader == null) {
				optionHeader = "";
			}
			
			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
		}
	}
}

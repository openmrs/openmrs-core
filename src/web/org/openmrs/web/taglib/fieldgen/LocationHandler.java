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

public class LocationHandler extends AbstractFieldGenHandler implements FieldGenHandler {
	
	private String defaultUrl = "location.field";
	
	public void run() {
		setUrl(defaultUrl);
		
		if (fieldGenTag != null) {
			String initialValue = "";
			checkEmptyVal((Location) null);
			Location l = (Location) this.fieldGenTag.getVal();
			if (l != null)
				if (l.getLocationId() != null)
					initialValue = l.getLocationId().toString();
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

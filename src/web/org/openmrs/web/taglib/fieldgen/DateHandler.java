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

public class DateHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "date.field";
	
	public void run() {
		setUrl(defaultUrl);
		
		String needScript = "true";
		
		if ( getRequest().getAttribute("org.openmrs.widget.dateField.needScript") != null ) {
			needScript = "false";
		} else {
			getRequest().setAttribute("org.openmrs.widget.dateField.needScript", "false");
		}
		
		setParameter("needScript", needScript);
	}
}

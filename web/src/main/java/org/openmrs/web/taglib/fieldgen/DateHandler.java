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

public class DateHandler extends AbstractFieldGenHandler implements FieldGenHandler {
	
	private String defaultUrl = "date.field";
	
	public void run() {
		setUrl(defaultUrl);
		
		String needScript = "true";
		
		if (getRequest().getAttribute("org.openmrs.widget.dateField.needScript") != null) {
			needScript = "false";
		} else {
			getRequest().setAttribute("org.openmrs.widget.dateField.needScript", "false");
		}
		
		setParameter("needScript", needScript);
	}
}

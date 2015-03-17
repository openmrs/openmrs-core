/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.layout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.layout.LayoutSupport;
import org.openmrs.layout.name.NameSupport;

public class NameLayoutPortletController extends LayoutPortletController {
	
	private static final Log log = LogFactory.getLog(NameLayoutPortletController.class);
	
	protected String getDefaultsPropertyName() {
		return "layout.name.defaults";
	}
	
	protected String getDefaultDivId() {
		return "nameLayoutPortlet";
	}
	
	protected LayoutSupport getLayoutSupportInstance() {
		log.debug("Getting name layout instance");
		return NameSupport.getInstance();
	}
}

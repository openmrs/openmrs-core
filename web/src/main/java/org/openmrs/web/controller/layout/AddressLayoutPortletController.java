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
import org.openmrs.layout.address.AddressSupport;

public class AddressLayoutPortletController extends LayoutPortletController {
	
	private static Log log = LogFactory.getLog(AddressLayoutPortletController.class);
	
	protected String getDefaultsPropertyName() {
		return "layout.address.defaults";
	}
	
	protected String getDefaultDivId() {
		return "addressLayoutPortlet";
	}
	
	protected LayoutSupport getLayoutSupportInstance() {
		log.debug("Getting address layout instance");
		return AddressSupport.getInstance();
	}
}

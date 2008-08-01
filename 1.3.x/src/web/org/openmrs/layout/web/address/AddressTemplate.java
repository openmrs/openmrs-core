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
package org.openmrs.layout.web.address;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.layout.web.LayoutSupport;
import org.openmrs.layout.web.LayoutTemplate;

public class AddressTemplate extends LayoutTemplate {
	private static Log log = LogFactory.getLog(AddressTemplate.class);
	
	public String getLayoutToken() {
		return "IS_ADDR_TOKEN";
	}
	
	public String getNonLayoutToken() {
		return "IS_NOT_ADDR_TOKEN";
	}

	@Override
	public LayoutSupport getLayoutSupportInstance() {
		return AddressSupport.getInstance();
	}
	
}

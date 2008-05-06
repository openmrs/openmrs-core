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
import org.openmrs.api.context.Context;
import org.openmrs.layout.web.LayoutSupport;

public class AddressSupport extends LayoutSupport<AddressTemplate> {

	private static AddressSupport singleton;
	
	static Log log = LogFactory.getLog(AddressSupport.class);
	
	public AddressSupport() {
		if (singleton == null)
			singleton = this;
		log.debug("Setting singleton: " + singleton);
	}
	
	public static AddressSupport getInstance() {
		if (singleton == null)
			throw new RuntimeException("Not Yet Instantiated");
		else {
			log.debug("Returning singleton: " + singleton);
			return singleton;			
		}
	}
	
	public String getDefaultLayoutFormat() {
			String ret = Context.getAdministrationService().getGlobalProperty("layout.address.format");
			return (ret != null && ret.length() > 0) ? ret : defaultLayoutFormat;
		}
}

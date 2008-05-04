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
package org.openmrs.layout.web.name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.layout.web.LayoutSupport;

public class NameSupport extends LayoutSupport<NameTemplate> {

	private static NameSupport singleton;
	
	static Log log = LogFactory.getLog(NameSupport.class);
	
	public NameSupport() {
		if (singleton == null)
			singleton = this;
	}
	
	public static NameSupport getInstance() {
		if (singleton == null)
			throw new RuntimeException("Not Yet Instantiated");
		else {
			return singleton;			
		}
	}
	
	public String getDefaultLayoutFormat() {
			String ret = Context.getAdministrationService().getGlobalProperty("layout.name.format");
			return (ret != null && ret.length() > 0) ? ret : defaultLayoutFormat;
		}
}

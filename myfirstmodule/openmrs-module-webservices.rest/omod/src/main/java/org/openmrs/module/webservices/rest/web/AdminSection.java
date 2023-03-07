/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;

/**
 * This class defines the links that will appear on the administration page under the
 * "basicmodule.title" heading. This extension is enabled by defining (uncommenting) it in the
 * /metadata/config.xml file.
 */
public class AdminSection extends AdministrationSectionExt {
	
	/**
	 * @see org.openmrs.module.web.extension.AdministrationSectionExt#getMediaType()
	 */
	@Override
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	/**
	 * @see org.openmrs.module.web.extension.AdministrationSectionExt#getTitle()
	 */
	@Override
	public String getTitle() {
		return RestConstants.MODULE_ID + ".title";
	}
	
	/**
	 * @see org.openmrs.module.web.extension.AdministrationSectionExt#getLinks()
	 */
	@Override
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		if (Context.hasPrivilege(RestConstants.PRIV_MANAGE_RESTWS)) {
			map.put("module/webservices/rest/settings.form", RestConstants.MODULE_ID + ".manage.settings");
		}
		
		if (Context.hasPrivilege(RestConstants.PRIV_VIEW_RESTWS) || Context.hasPrivilege(RestConstants.PRIV_MANAGE_RESTWS)) {
			map.put("module/webservices/rest/test.htm", RestConstants.MODULE_ID + ".test");
			map.put("module/webservices/rest/apiDocs.htm", RestConstants.MODULE_ID + ".swaggerDocumentation");
			
		}
		
		return map;
	}
	
}

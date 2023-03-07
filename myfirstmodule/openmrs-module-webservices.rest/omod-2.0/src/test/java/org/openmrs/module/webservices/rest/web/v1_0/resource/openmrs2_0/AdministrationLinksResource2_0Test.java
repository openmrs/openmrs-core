/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0;

import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.AdministrationSectionLinks;

import java.util.HashMap;
import java.util.Map;

public class AdministrationLinksResource2_0Test
		extends BaseDelegatingResourceTest<AdministrationLinksResource2_0, AdministrationSectionLinks> {

	@Override
	public AdministrationSectionLinks newObject() {
		Map<String, String> links = new HashMap<>();
		links.put("module/webservices/rest/settings.form", RestConstants.MODULE_ID + ".manage.settings");

		return new AdministrationSectionLinks(RestConstants.MODULE_ID,
				RestConstants.MODULE_ID + ".title", links);
	}

	@Override
	public String getDisplayProperty() {
		return RestConstants.MODULE_ID + ".title";
	}

	@Override
	public String getUuidProperty() {
		return RestConstants.MODULE_ID;
	}
}

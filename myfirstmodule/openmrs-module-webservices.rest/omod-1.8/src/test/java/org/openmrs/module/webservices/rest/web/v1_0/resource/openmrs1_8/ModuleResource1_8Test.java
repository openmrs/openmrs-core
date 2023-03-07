/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import org.openmrs.module.Module;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

/**
 *
 */
public class ModuleResource1_8Test extends BaseDelegatingResourceTest<ModuleResource1_8, Module> {
	
	@Override
	public Module newObject() {
		return new Module("Atlas Module", "atlas", "name", "author", "description", "version");
	}
	
	@Override
	public String getDisplayProperty() {
		return "Atlas Module";
	}
	
	@Override
	public String getUuidProperty() {
		return "atlas";
	}
}

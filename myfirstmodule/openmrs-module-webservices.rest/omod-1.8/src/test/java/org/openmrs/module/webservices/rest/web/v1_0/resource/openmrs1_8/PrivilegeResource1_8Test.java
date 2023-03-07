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

import java.util.UUID;

import org.openmrs.Privilege;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class PrivilegeResource1_8Test extends BaseDelegatingResourceTest<PrivilegeResource1_8, Privilege> {
	
	private String uuid;
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#newObject()
	 */
	@Override
	public Privilege newObject() {
		Privilege privilege = new Privilege("PrivilegeResourceTest Privilege", "This privilege is only for testing.");
		privilege.setUuid(UUID.randomUUID().toString()); //Uuid isn't assigned during creation until 1.8.1. 
		uuid = privilege.getUuid();
		Context.getUserService().savePrivilege(privilege);
		return privilege;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getDisplayProperty()
	 */
	@Override
	public String getDisplayProperty() {
		return "PrivilegeResourceTest Privilege";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getUuidProperty()
	 */
	@Override
	public String getUuidProperty() {
		return uuid;
	}
}

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

import org.openmrs.Role;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class RoleResource1_8Test extends BaseDelegatingResourceTest<RoleResource1_8, Role> {
	
	@Override
	public Role newObject() {
		Role role = Context.getUserService().getRoleByUuid(getUuidProperty());
		return role;
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("privileges");
		assertPropPresent("inheritedRoles");
		assertPropEquals("retired", getObject().getRetired());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("privileges");
		assertPropPresent("inheritedRoles");
		assertPropPresent("allInheritedRoles");
		assertPropEquals("retired", getObject().getRetired());
	}
	
	@Override
	public String getDisplayProperty() {
		return "Provider";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.ROLE_UUID;
	}
}

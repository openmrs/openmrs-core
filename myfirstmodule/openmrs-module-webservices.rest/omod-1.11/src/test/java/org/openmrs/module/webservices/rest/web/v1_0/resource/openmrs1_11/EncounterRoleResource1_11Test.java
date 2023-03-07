/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11;

import org.openmrs.EncounterRole;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_11;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class EncounterRoleResource1_11Test extends BaseDelegatingResourceTest<EncounterRoleResource1_11, EncounterRole> {
	
	@Override
	public EncounterRole newObject() {
		return Context.getEncounterService().getEncounterRoleByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("display", getDisplayProperty());
		getObject().toString();
		assertPropEquals("uuid", getObject().getUuid());
		assertPropPresent("links");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("dateCreated", getObject().getDateChanged());
		assertPropEquals("retired", getObject().getRetired());
		assertPropEquals("name", getObject().getName());
		assertPropPresent("auditInfo");
		assertPropPresent("retired");
		assertPropPresent("resourceVersion");
	}
	
	@Override
	public String getDisplayProperty() {
		return "Unknown";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_11.ENCOUNTER_ROLE_UUID;
	}
}

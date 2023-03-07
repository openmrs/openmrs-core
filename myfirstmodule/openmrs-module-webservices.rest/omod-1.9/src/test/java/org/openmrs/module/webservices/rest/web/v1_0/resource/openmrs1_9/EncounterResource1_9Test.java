/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

/**
 * Contains tests for the {@link EncounterResource1_9}
 */
public class EncounterResource1_9Test extends BaseDelegatingResourceTest<EncounterResource1_9, Encounter> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#newObject()
	 */
	@Override
	public Encounter newObject() {
		return Context.getEncounterService().getEncounterByUuid(getUuidProperty());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#validateDefaultRepresentation()
	 */
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("encounterDatetime", getObject().getEncounterDatetime());
		assertPropPresent("patient");
		assertPropPresent("location");
		assertPropPresent("form");
		assertPropPresent("encounterType");
		assertPropPresent("obs");
		assertPropPresent("orders");
		assertPropPresent("encounterProviders");
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("visit");
		assertPropEquals("resourceVersion", "1.9");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#validateFullRepresentation()
	 */
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("encounterDatetime", getObject().getEncounterDatetime());
		assertPropEquals("encounterDatetime", getObject().getEncounterDatetime());
		assertPropPresent("patient");
		assertPropPresent("location");
		assertPropPresent("form");
		assertPropPresent("encounterType");
		assertPropPresent("encounterProviders");
		assertPropPresent("obs");
		assertPropPresent("orders");
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("auditInfo");
		assertPropPresent("visit");
		assertPropEquals("resourceVersion", "1.9");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getDisplayProperty()
	 */
	@Override
	public String getDisplayProperty() {
		return "Emergency 01/08/2008";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getUuidProperty()
	 */
	@Override
	public String getUuidProperty() {
		return "6519d653-393b-4118-9c83-a3715b82d4ac";
	}
}

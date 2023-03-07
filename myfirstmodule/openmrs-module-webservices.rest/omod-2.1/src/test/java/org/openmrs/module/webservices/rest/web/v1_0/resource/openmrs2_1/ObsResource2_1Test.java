/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_1;

import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class ObsResource2_1Test extends BaseDelegatingResourceTest<ObsResource2_1, Obs> {
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("status");
		assertPropPresent("interpretation");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("status");
		assertPropPresent("interpretation");
	}
	
	@Override
	public Obs newObject() {
		return Context.getObsService().getObsByUuid(getUuidProperty());
	}
	
	@Override
	public String getDisplayProperty() {
		return "WEIGHT (KG): 50.0";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.OBS_UUID;
	}
	
}

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

import org.junit.Before;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class LocationResource1_9Test extends BaseDelegatingResourceTest<LocationResource1_9, Location> {
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants1_9.TEST_DATASET);
	}
	
	@Override
	public Location newObject() {
		return Context.getLocationService().getLocationByUuid(getUuidProperty());
	}
	
	@Override
	public void validateRefRepresentation() throws Exception {
		super.validateRefRepresentation();
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("address1", getObject().getAddress1());
		assertPropEquals("address2", getObject().getAddress2());
		assertPropEquals("cityVillage", getObject().getCityVillage());
		assertPropEquals("stateProvince", getObject().getStateProvince());
		assertPropEquals("country", getObject().getCountry());
		assertPropEquals("postalCode", getObject().getPostalCode());
		assertPropEquals("latitude", getObject().getLatitude());
		assertPropEquals("longitude", getObject().getLongitude());
		assertPropEquals("countyDistrict", getObject().getCountyDistrict());
		assertPropEquals("address3", getObject().getAddress3());
		assertPropEquals("address4", getObject().getAddress4());
		assertPropEquals("address5", getObject().getAddress5());
		assertPropEquals("address6", getObject().getAddress6());
		assertPropPresent("tags");
		assertPropPresent("attributes");
		assertPropPresent("parentLocation");
		assertPropPresent("childLocations");
		assertPropEquals("retired", getObject().isRetired());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("address1", getObject().getAddress1());
		assertPropEquals("address2", getObject().getAddress2());
		assertPropEquals("cityVillage", getObject().getCityVillage());
		assertPropEquals("stateProvince", getObject().getStateProvince());
		assertPropEquals("country", getObject().getCountry());
		assertPropEquals("postalCode", getObject().getPostalCode());
		assertPropEquals("latitude", getObject().getLatitude());
		assertPropEquals("longitude", getObject().getLongitude());
		assertPropEquals("countyDistrict", getObject().getCountyDistrict());
		assertPropEquals("address3", getObject().getAddress3());
		assertPropEquals("address4", getObject().getAddress4());
		assertPropEquals("address5", getObject().getAddress5());
		assertPropEquals("address6", getObject().getAddress6());
		assertPropPresent("tags");
		assertPropPresent("attributes");
		assertPropPresent("parentLocation");
		assertPropPresent("childLocations");
		assertPropEquals("retired", getObject().isRetired());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "Mumbai";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_9.LOCATION_UUID;
	}
	
}

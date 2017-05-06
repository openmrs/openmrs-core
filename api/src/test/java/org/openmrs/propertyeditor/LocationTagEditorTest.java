/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import org.junit.Before;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.springframework.beans.factory.annotation.Autowired;

public class LocationTagEditorTest extends BasePropertyEditorTest<LocationTag, LocationTagEditor> {
	
	protected static final String LOC_INITIAL_DATA_XML = "org/openmrs/api/include/LocationServiceTest-initialData.xml";
	
	private static final Integer EXISTING_ID = 1;
	
	@Autowired
	private LocationService locationService;
	
	@Before
	public void prepareData() {
		executeDataSet(LOC_INITIAL_DATA_XML);
	}
	
	@Override
	protected LocationTagEditor getNewEditor() {
		return new LocationTagEditor();
	}
	
	@Override
	protected LocationTag getExistingObject() {
		return locationService.getLocationTag(EXISTING_ID);
	}
}

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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class LocationEditorTest extends BaseContextSensitiveTest {

	LocationEditor editor;

	@Autowired
	private LocationService locationService;
	
	/**
	 * @see LocationEditor#setAsText(String)
	 */

	@Before
	public void before() {
		editor = new LocationEditor(); 
	}

	@Test
	public void setAsText_shouldSetUsingId() {
		editor.setAsText("1");
		Location expectedLocation = locationService.getLocation(1);
		Assert.assertEquals(expectedLocation, editor.getValue());
	}
	
	/**
	 * @see LocationEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() {
		editor.setAsText("8d6c993e-c2cc-11de-8d13-0010c6dffd0f");
		Location expectedLocation = locationService.getLocationByUuid("8d6c993e-c2cc-11de-8d13-0010c6dffd0f");
		Assert.assertEquals(expectedLocation, editor.getValue());
	}

	@Test
	public void getAsText_shouldGetNullIfTextNull() {
		Assert.assertNull(editor.getAsText());
	}

	@Test
	public void getAsText_shouldGetTextIfTextNotNull() {
		editor.setAsText("1");
		Assert.assertNotNull(editor.getAsText());
	}
}

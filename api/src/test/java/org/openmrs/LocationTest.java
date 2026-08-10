/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.junit.jupiter.api.Test;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocationTest extends BaseContextSensitiveTest {

	/**
	 * Get locations that have any of specified set of tags among its child tags.
	 *
	 * @see Location#isInHierarchy(Location,Location)
	 */
	@Test
	public void isInHierarchy_shouldShouldFindLocationInHierarchy() {
		Location locationGrandParent = new Location();
		Location locationParent = new Location();
		Location locationChild = new Location();

		locationGrandParent.addChildLocation(locationParent);
		locationParent.addChildLocation(locationChild);

		assertTrue(Location.isInHierarchy(locationChild, locationParent));
		assertTrue(Location.isInHierarchy(locationChild, locationGrandParent));
	}

	/**
	 * @see Location#removeChildLocation(Location)
	 */
	@Test
	public void removeChildLocation_shouldNotDeleteLocationWhenReparenting() {
		executeDataSet("org/openmrs/api/include/LocationServiceTest-initialData.xml");
		LocationService locationService = Context.getLocationService();

		Location child = locationService.getLocation(2);
		Location oldParent = child.getParentLocation();
		Location newParent = locationService.getLocation(3);

		// Removing the child should only change the relationship, not delete the location.
		oldParent.removeChildLocation(child);
		child.setParentLocation(newParent);
		locationService.saveLocation(child);

		Context.flushSession();
		Context.clearSession();

		// Reload the child to verify that it still exists after the relationship change.
		Location reloadedChild = locationService.getLocation(child.getLocationId());

		assertNotNull(reloadedChild);
		assertEquals(newParent.getLocationId(), reloadedChild.getParentLocation().getLocationId());
	}

}

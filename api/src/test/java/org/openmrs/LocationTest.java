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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openmrs.test.Verifies;

public class LocationTest {
	
	/**
	 * Get locations that have any of specified set of tags among its child tags.
	 * 
	 * @see {@link Location#isInHierarchy(Location,Location)}
	 */
	@Test
	@Verifies(value = "should should find location in hierarchy", method = "isInHierarchy(Location,Location)")
	public void isInHierarchy_shouldShouldFindLocationInHierarchy() throws Exception {
		Location locationGrandParent = new Location();
		Location locationParent = new Location();
		Location locationChild = new Location();
		
		locationGrandParent.addChildLocation(locationParent);
		locationParent.addChildLocation(locationChild);
		
		assertTrue(Location.isInHierarchy(locationChild, locationParent));
		assertTrue(Location.isInHierarchy(locationChild, locationGrandParent));
	}
	
}

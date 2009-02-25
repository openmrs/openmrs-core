/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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

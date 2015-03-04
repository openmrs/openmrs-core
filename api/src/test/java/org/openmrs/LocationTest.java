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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
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
	
	@Test
	public void getDescendantLocations_shouldReturnAllDescendantLocationsIfIncludeRetiredIsTrue() {
		
		Location rootLocation = new Location();
		//first level
		Location locationOne = new Location();
		Location locationTwo = new Location();
		//second level
		Location childOflocationOne = new Location();
		Location childOnfLocationTwo = new Location();
		
		//make child-parent relations
		rootLocation.setChildLocations(new HashSet<Location>(Arrays.asList(locationOne, locationTwo)));
		
		locationOne.setChildLocations(new HashSet<Location>(Arrays.asList(childOflocationOne)));
		locationTwo.setChildLocations(new HashSet<Location>(Arrays.asList(childOnfLocationTwo)));
		
		childOflocationOne.setChildLocations(new HashSet<Location>());
		childOnfLocationTwo.setChildLocations(new HashSet<Location>());
		
		Set<Location> descendantLocations = rootLocation.getDescendantLocations(true);
		
		Set<Location> expectedLocations = new HashSet<Location>(Arrays.asList(locationOne, locationTwo, childOflocationOne,
		    childOnfLocationTwo));
		Assert.assertThat(descendantLocations, equalTo(expectedLocations));
		
	}
	
	@Test
	public void getDescendantLocations_shouldReturnNonRetiredDescendantLocationsIfIncludeRetiredIsFalse() {
		
		Location rootLocation = new Location();
		//first level
		Location nonRetiredLocation = new Location();
		Location retiredLocation = new Location();
		retiredLocation.setRetired(true);
		//second level
		Location firstChildOfNonRetiredLocation = new Location();
		Location secondChildOfNonRetiredLocation = new Location();
		
		Location firstChildOfRetiredLocation = new Location();
		
		//make child-parent relations
		rootLocation.setChildLocations(new HashSet<Location>(Arrays.asList(nonRetiredLocation, retiredLocation)));
		
		nonRetiredLocation.setChildLocations(new HashSet<Location>(Arrays.asList(firstChildOfNonRetiredLocation,
		    secondChildOfNonRetiredLocation)));
		retiredLocation.setChildLocations(new HashSet<Location>(Arrays.asList(firstChildOfRetiredLocation)));
		
		firstChildOfNonRetiredLocation.setChildLocations(new HashSet<Location>());
		secondChildOfNonRetiredLocation.setChildLocations(new HashSet<Location>());
		
		firstChildOfRetiredLocation.setChildLocations(new HashSet<Location>());
		
		//action
		Set<Location> descendantLocations = rootLocation.getDescendantLocations(false);
		
		Set<Location> expectedLocations = new HashSet<Location>(Arrays.asList(nonRetiredLocation,
		    firstChildOfNonRetiredLocation, secondChildOfNonRetiredLocation));
		
		Assert.assertThat(descendantLocations, equalTo(expectedLocations));
	}
	
}

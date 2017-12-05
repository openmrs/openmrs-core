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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class LocationTest {
	
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
		rootLocation.setChildLocations(new HashSet<>(Arrays.asList(locationOne, locationTwo)));
		
		locationOne.setChildLocations(new HashSet<>(Collections.singletonList(childOflocationOne)));
		locationTwo.setChildLocations(new HashSet<>(Collections.singletonList(childOnfLocationTwo)));
		
		childOflocationOne.setChildLocations(new HashSet<>());
		childOnfLocationTwo.setChildLocations(new HashSet<>());
		
		Set<Location> descendantLocations = rootLocation.getDescendantLocations(true);
		
		Set<Location> expectedLocations = new HashSet<>(Arrays.asList(locationOne, locationTwo, childOflocationOne,
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
		rootLocation.setChildLocations(new HashSet<>(Arrays.asList(nonRetiredLocation, retiredLocation)));
		
		nonRetiredLocation.setChildLocations(new HashSet<>(Arrays.asList(firstChildOfNonRetiredLocation,
		    secondChildOfNonRetiredLocation)));
		retiredLocation.setChildLocations(new HashSet<>(Collections.singletonList(firstChildOfRetiredLocation)));
		
		firstChildOfNonRetiredLocation.setChildLocations(new HashSet<>());
		secondChildOfNonRetiredLocation.setChildLocations(new HashSet<>());
		
		firstChildOfRetiredLocation.setChildLocations(new HashSet<>());
		
		//action
		Set<Location> descendantLocations = rootLocation.getDescendantLocations(false);
		
		Set<Location> expectedLocations = new HashSet<>(Arrays.asList(nonRetiredLocation,
		    firstChildOfNonRetiredLocation, secondChildOfNonRetiredLocation));
		
		Assert.assertThat(descendantLocations, equalTo(expectedLocations));
	}
}

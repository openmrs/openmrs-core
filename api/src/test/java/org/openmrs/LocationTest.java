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

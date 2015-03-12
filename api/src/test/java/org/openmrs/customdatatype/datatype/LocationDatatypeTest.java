/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype.datatype;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;

public class LocationDatatypeTest {
	
	LocationDatatype datatype;
	
	@Before
	public void before() {
		datatype = new LocationDatatype();
	}
	
	/**
	 * @see Location#deserialize(String)
	 * @verifies reconstruct a location serialized by this handler
	 */
	@Test
	public void deserialize_shouldReconstructASimplestLocationSerializedByThisHandler() throws Exception {
		Location location = new Location(123);
		Assert.assertEquals(location, datatype.deserialize(datatype.serialize(location)));
	}
	
	/**
	 * @see Location#serialize(java.util.Date)
	 * @verifies compare equals of source and reconstructed location
	 */
	@Test
	public void serialize_shouldBeEqualsSourceAndReconstructedLocation() throws Exception {
		Integer id = 123;
		String address1 = "Miami";
		String address2 = "Boston";
		String address3 = "New York";
		String country = "USA";
		String name = "United States location";
		
		Location location = new Location(id);
		location.setAddress1(address1);
		location.setAddress2(address2);
		location.setAddress3(address3);
		location.setCountry(country);
		location.setName(name);
		
		Location deserializedLocation = datatype.deserialize(datatype.serialize(location));
		
		Assert.assertEquals(location.getId(), deserializedLocation.getId());
		Assert.assertEquals(location.getAddress1(), deserializedLocation.getAddress1());
		Assert.assertEquals(location.getAddress2(), deserializedLocation.getAddress2());
		Assert.assertEquals(location.getAddress3(), deserializedLocation.getAddress3());
		Assert.assertEquals(location.getCountry(), deserializedLocation.getCountry());
		Assert.assertEquals(location.getName(), deserializedLocation.getName());
	}
}

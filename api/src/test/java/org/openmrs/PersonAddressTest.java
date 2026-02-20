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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Date;

import org.junit.jupiter.api.Test;

public class PersonAddressTest {
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyAddressOneDiffers() {
		PersonAddress rileyStreetAddress = new PersonAddress();
		PersonAddress crownStreetAddress = new PersonAddress();
		crownStreetAddress.setAddress1("crown street");
		rileyStreetAddress.setAddress1("riley street");
		
		assertThat(crownStreetAddress.equalsContent(rileyStreetAddress), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyAddressTwoDiffers() {
		PersonAddress rileyStreetAddress = new PersonAddress();
		PersonAddress crownStreetAddress = new PersonAddress();
		crownStreetAddress.setAddress2("crown street");
		rileyStreetAddress.setAddress2("riley street");
		
		assertThat(crownStreetAddress.equalsContent(rileyStreetAddress), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyAddressThreeDiffers() {
		PersonAddress rileyStreetAddress = new PersonAddress();
		PersonAddress crownStreetAddress = new PersonAddress();
		crownStreetAddress.setAddress3("crown street");
		rileyStreetAddress.setAddress3("riley street");
		
		assertThat(crownStreetAddress.equalsContent(rileyStreetAddress), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyAddressFourDiffers() {
		PersonAddress rileyStreetAddress = new PersonAddress();
		PersonAddress crownStreetAddress = new PersonAddress();
		crownStreetAddress.setAddress4("crown street");
		rileyStreetAddress.setAddress4("riley street");
		
		assertThat(crownStreetAddress.equalsContent(rileyStreetAddress), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyAddressFiveDiffers() {
		PersonAddress rileyStreetAddress = new PersonAddress();
		PersonAddress crownStreetAddress = new PersonAddress();
		crownStreetAddress.setAddress5("crown street");
		rileyStreetAddress.setAddress5("riley street");
		
		assertThat(crownStreetAddress.equalsContent(rileyStreetAddress), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyAddressSixDiffers() {
		PersonAddress rileyStreetAddress = new PersonAddress();
		PersonAddress crownStreetAddress = new PersonAddress();
		crownStreetAddress.setAddress6("crown street");
		rileyStreetAddress.setAddress6("riley street");
		
		assertThat(crownStreetAddress.equalsContent(rileyStreetAddress), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyCityVillageDiffers() {
		PersonAddress address1 = new PersonAddress();
		PersonAddress address2 = new PersonAddress();
		address2.setCityVillage("faketown");
		address1.setCityVillage("realtown");
		
		assertThat(address2.equalsContent(address1), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyCountyDistrictDiffers() {
		PersonAddress address1 = new PersonAddress();
		PersonAddress address2 = new PersonAddress();
		address2.setCountyDistrict("fancy county");
		address1.setCountyDistrict("omaha county");
		
		assertThat(address2.equalsContent(address1), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyCountryDiffers() {
		PersonAddress address1 = new PersonAddress();
		PersonAddress address2 = new PersonAddress();
		address2.setCountry("australia");
		address1.setCountry("zimbabwe");
		
		assertThat(address2.equalsContent(address1), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyPostalCodeDiffers() {
		PersonAddress address1 = new PersonAddress();
		PersonAddress address2 = new PersonAddress();
		address2.setPostalCode("99999");
		address1.setPostalCode("243234");
		
		assertThat(address2.equalsContent(address1), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyStateProvinceDiffers() {
		PersonAddress address1 = new PersonAddress();
		PersonAddress address2 = new PersonAddress();
		address2.setCountry("victoria");
		address1.setCountry("tasmania");
		
		assertThat(address2.equalsContent(address1), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyLatitudeDiffers() {
		PersonAddress address1 = new PersonAddress();
		PersonAddress address2 = new PersonAddress();
		address2.setLatitude("-23.33");
		address1.setLatitude("43.3");
		
		assertThat(address2.equalsContent(address1), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyLongitudeDiffers() {
		PersonAddress address1 = new PersonAddress();
		PersonAddress address2 = new PersonAddress();
		address2.setLongitude("-23.33");
		address1.setLongitude("43.3");
		
		assertThat(address2.equalsContent(address1), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyStartDateDiffers() {
		PersonAddress address1 = new PersonAddress();
		PersonAddress address2 = new PersonAddress();
		address2.setStartDate(new Date(123L));
		address1.setStartDate(new Date(1000000L));
		
		assertThat(address2.equalsContent(address1), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyEndDateDiffers() {
		PersonAddress address1 = new PersonAddress();
		PersonAddress address2 = new PersonAddress();
		address2.setStartDate(new Date(123L));
		address1.setStartDate(new Date(1000000L));

		assertThat(address2.equalsContent(address1), is(false));
	}

	@Test
	void copy_shouldReturnNewInstance() {
		PersonAddress original = new PersonAddress();
		original.setAddress1("123 Main St");

		PersonAddress copy = original.copy();

		assertNotSame(original, copy);
	}

	@Test
	void copy_shouldCopyAllAddressFields() {
		PersonAddress original = new PersonAddress();
		original.setAddress1("123 Main St");
		original.setAddress2("Apt 4");
		original.setAddress3("Building B");
		original.setCityVillage("Springfield");
		original.setStateProvince("IL");
		original.setCountry("USA");
		original.setPostalCode("62701");
		original.setLatitude("39.7817");
		original.setLongitude("-89.6501");
		original.setStartDate(new Date(1000L));
		original.setEndDate(new Date(2000L));
		original.setPreferred(true);

		PersonAddress copy = original.copy();

		assertEquals(original.getAddress1(), copy.getAddress1());
		assertEquals(original.getAddress2(), copy.getAddress2());
		assertEquals(original.getAddress3(), copy.getAddress3());
		assertEquals(original.getCityVillage(), copy.getCityVillage());
		assertEquals(original.getStateProvince(), copy.getStateProvince());
		assertEquals(original.getCountry(), copy.getCountry());
		assertEquals(original.getPostalCode(), copy.getPostalCode());
		assertEquals(original.getLatitude(), copy.getLatitude());
		assertEquals(original.getLongitude(), copy.getLongitude());
		assertEquals(original.getStartDate(), copy.getStartDate());
		assertEquals(original.getEndDate(), copy.getEndDate());
		assertEquals(original.getPreferred(), copy.getPreferred());
	}

	@Test
	void copy_shouldSharePersonReference() {
		Person person = new Person();
		PersonAddress original = new PersonAddress();
		original.setPerson(person);

		PersonAddress copy = original.copy();

		assertSame(original.getPerson(), copy.getPerson());
	}

}

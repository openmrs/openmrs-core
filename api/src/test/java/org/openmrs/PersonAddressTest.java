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

import org.junit.Test;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
		address2.setStartDate(new Date(123l));
		address1.setStartDate(new Date(1000000l));
		
		assertThat(address2.equalsContent(address1), is(false));
	}
	
	@Test
	public void equalsContent_shouldIndicateUnequalWhenOnlyEndDateDiffers() {
		PersonAddress address1 = new PersonAddress();
		PersonAddress address2 = new PersonAddress();
		address2.setStartDate(new Date(123l));
		address1.setStartDate(new Date(1000000l));
		
		assertThat(address2.equalsContent(address1), is(false));
	}
	
}

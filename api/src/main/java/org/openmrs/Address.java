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

/**
 * Defines the standard fields for an Address in OpenMRS
 * @since 1.7.2
 */
public interface Address {
	
	public String getAddress1();
	
	public void setAddress1(String address1);
	
	public String getAddress2();
	
	public void setAddress2(String address2);
	
	public String getAddress3();
	
	public void setAddress3(String address3);
	
	public String getAddress4();
	
	public void setAddress4(String address4);
	
	public String getAddress5();
	
	public void setAddress5(String address5);
	
	public String getAddress6();
	
	public void setAddress6(String address6);
	
	public String getCityVillage();
	
	public void setCityVillage(String cityVillage);
	
	public String getStateProvince();
	
	public void setStateProvince(String stateProvince);
	
	public String getCountyDistrict();
	
	public void setCountyDistrict(String countyDistrict);
	
	public String getPostalCode();
	
	public void setPostalCode(String postalCode);
	
	public String getCountry();
	
	public void setCountry(String country);
	
	public String getLatitude();
	
	public void setLatitude(String latitude);
	
	public String getLongitude();
	
	public void setLongitude(String longitude);
}

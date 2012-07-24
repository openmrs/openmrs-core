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

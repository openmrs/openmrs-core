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
	
	/**
	 * @since 2.0
	 * @return the address7
	 */
	String getAddress7();
	
	/**
	 * @since 2.0
	 * @param address7
	 */
	void setAddress7(String address7);
	
	/**
	 * @since 2.0
	 * @return the address8
	 */
	String getAddress8();
	
	/**
	 * @since 2.0
	 * @param address8
	 */
	void setAddress8(String address8);
	
	/**
	 * @since 2.0
	 * @return the address9
	 */
	String getAddress9();
	
	/**
	 * @since 2.0
	 * @param address9
	 */
	void setAddress9(String address9);
	
	/**
	 * @since 2.0
	 * @return the address10
	 */
	String getAddress10();
	
	/**
	 * @since 2.0
	 * @param address10
	 */
	void setAddress10(String address10);
	
	/**
	 * @since 2.0
	 * @return the address11
	 */
	String getAddress11();
	
	/**
	 * @since 2.0
	 * @param address11
	 */
	void setAddress11(String address11);
	
	/**
	 * @since 2.0
	 * @return the address12
	 */
	String getAddress12();
	
	/**
	 * @since 2.0
	 * @param address12
	 */
	void setAddress12(String address12);
	
	/**
	 * @since 2.0
	 * @return the address13
	 */
	String getAddress13();
	
	/**
	 * @since 2.0
	 * @param address13
	 */
	void setAddress13(String address13);
	
	/**
	 * @since 2.0
	 * @return the address14
	 */
	String getAddress14();
	
	/**
	 * @since 2.0
	 * @param address14
	 */
	void setAddress14(String address14);
	
	/**
	 * @since 2.0
	 * @return the address15
	 */
	String getAddress15();
	
	/**
	 * @since 2.0
	 * @param address15
	 */
	void setAddress15(String address15);
	
}

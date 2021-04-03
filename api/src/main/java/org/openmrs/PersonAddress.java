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

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.openmrs.util.OpenmrsUtil;

/**
 * This class is the representation of a person's address. This class is many-to-one to the Person
 * class, so a Person/Patient/User can have zero to n addresses
 */
public class PersonAddress extends BaseChangeableOpenmrsData implements java.io.Serializable, Cloneable, Comparable<PersonAddress>, Address {
	
	public static final long serialVersionUID = 343333L;
	
	// Fields

	private Integer personAddressId;

	private Person person;
	
	private Boolean preferred = false;

	private String address1;

	private String address2;

	private String address3;

	private String address4;

	private String address5;

	private String address6;

	private String address7;

	private String address8;

	private String address9;

	private String address10;

	private String address11;

	private String address12;

	private String address13;

	private String address14;

	private String address15;

	private String cityVillage;

	private String countyDistrict;

	private String stateProvince;

	private String country;

	private String postalCode;

	private String latitude;

	private String longitude;
	
	private Date startDate;
	
	private Date endDate;
	
	// Constructors
	
	/** default constructor */
	public PersonAddress() {
	}
	
	/** constructor with id */
	public PersonAddress(Integer personAddressId) {
		this.personAddressId = personAddressId;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "a1:" + getAddress1() + ", a2:" + getAddress2() + ", cv:" +
				getCityVillage() + ", sp:" + getStateProvince() + ", c:" + getCountry() +
				", cd:" + getCountyDistrict() + ", nc:" + getAddress3() + ", pc:" +
				getPostalCode() + ", lat:" + getLatitude() + ", long:" + getLongitude();
	}
	
	/**
	 * Compares this PersonAddress object to the given otherAddress. This method differs from
	 * {@link #equals(Object)} in that this method compares the inner fields of each address for
	 * equality. Note: Null/empty fields on <code>otherAddress</code> /will not/ cause a false value
	 * to be returned
	 *
	 * @param otherAddress PersonAddress with which to compare
	 * @return boolean true/false whether or not they are the same addresses
	 */
	public boolean equalsContent(PersonAddress otherAddress) {
		return new EqualsBuilder().append(defaultString(otherAddress.getAddress1()), defaultString(address1)).append(
		    defaultString(otherAddress.getAddress2()), defaultString(address2)).append(
		    defaultString(otherAddress.getAddress3()), defaultString(address3)).append(
		    defaultString(otherAddress.getAddress4()), defaultString(address4)).append(
		    defaultString(otherAddress.getAddress5()), defaultString(address5)).append(
		    defaultString(otherAddress.getAddress6()), defaultString(address6)).append(
		    defaultString(otherAddress.getAddress7()), defaultString(address7)).append(
		    defaultString(otherAddress.getAddress8()), defaultString(address8)).append(
		    defaultString(otherAddress.getAddress9()), defaultString(address9)).append(
		    defaultString(otherAddress.getAddress10()), defaultString(address10)).append(
		    defaultString(otherAddress.getAddress11()), defaultString(address11)).append(
		    defaultString(otherAddress.getAddress12()), defaultString(address12)).append(
		    defaultString(otherAddress.getAddress13()), defaultString(address13)).append(
		    defaultString(otherAddress.getAddress14()), defaultString(address14)).append(
		    defaultString(otherAddress.getAddress15()), defaultString(address15)).append(
		    defaultString(otherAddress.getCityVillage()), defaultString(cityVillage)).append(
		    defaultString(otherAddress.getCountyDistrict()), defaultString(countyDistrict)).append(
		    defaultString(otherAddress.getStateProvince()), defaultString(stateProvince)).append(
		    defaultString(otherAddress.getCountry()), defaultString(country)).append(
		    defaultString(otherAddress.getPostalCode()), defaultString(postalCode)).append(
		    defaultString(otherAddress.getLatitude()), defaultString(latitude)).append(
		    defaultString(otherAddress.getLongitude()), defaultString(longitude)).append(otherAddress.getStartDate(),
		    startDate).append(otherAddress.getEndDate(), endDate).isEquals();
	}
	
	/**
	 * bitwise copy of the personAddress object. NOTICE: THIS WILL NOT COPY THE PATIENT OBJECT. The
	 * PersonAddress.person object in this object AND the cloned object will point at the same
	 * person
	 *
	 * @return New PersonAddress object
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError("PersonAddress should be cloneable");
		}
	}
	
	/**
	 * @return Returns the address1.
	 */
	@Override
	public String getAddress1() {
		return address1;
	}
	
	/**
	 * @param address1 The address1 to set.
	 */
	@Override
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	
	/**
	 * @return Returns the address2.
	 */
	@Override
	public String getAddress2() {
		return address2;
	}
	
	/**
	 * @param address2 The address2 to set.
	 */
	@Override
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	
	/**
	 * @return Returns the cityVillage.
	 */
	@Override
	public String getCityVillage() {
		return cityVillage;
	}
	
	/**
	 * @param cityVillage The cityVillage to set.
	 */
	@Override
	public void setCityVillage(String cityVillage) {
		this.cityVillage = cityVillage;
	}
	
	/**
	 * @return Returns the country.
	 */
	@Override
	public String getCountry() {
		return country;
	}
	
	/**
	 * @param country The country to set.
	 */
	@Override
	public void setCountry(String country) {
		this.country = country;
	}
	
	/**
	 * @return Returns the preferred.
	 * 
	 * @deprecated as of 2.0, use {@link #getPreferred()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isPreferred() {
		return getPreferred();
	}
	
	public Boolean getPreferred() {
		return preferred == null ? Boolean.FALSE : preferred;
	}
	
	/**
	 * @param preferred The preferred to set.
	 */
	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}
	
	/**
	 * @return Returns the latitude.
	 */
	@Override
	public String getLatitude() {
		return latitude;
	}
	
	/**
	 * @param latitude The latitude to set.
	 */
	@Override
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	/**
	 * @return Returns the longitude.
	 */
	@Override
	public String getLongitude() {
		return longitude;
	}
	
	/**
	 * @param longitude The longitude to set.
	 */
	@Override
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	/**
	 * @return Returns the person.
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * @param person The person to set.
	 */
	public void setPerson(Person person) {
		this.person = person;
	}
	
	/**
	 * @return Returns the personAddressId.
	 */
	public Integer getPersonAddressId() {
		return personAddressId;
	}
	
	/**
	 * @param personAddressId The personAddressId to set.
	 */
	public void setPersonAddressId(Integer personAddressId) {
		this.personAddressId = personAddressId;
	}
	
	/**
	 * @return Returns the postalCode.
	 */
	@Override
	public String getPostalCode() {
		return postalCode;
	}
	
	/**
	 * @param postalCode The postalCode to set.
	 */
	@Override
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	
	/**
	 * @return Returns the stateProvince.
	 */
	@Override
	public String getStateProvince() {
		return stateProvince;
	}
	
	/**
	 * @param stateProvince The stateProvince to set.
	 */
	@Override
	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}
	
	/**
	 * @return Returns the countyDistrict.
	 */
	@Override
	public String getCountyDistrict() {
		return countyDistrict;
	}
	
	/**
	 * @param countyDistrict The countyDistrict to set.
	 */
	@Override
	public void setCountyDistrict(String countyDistrict) {
		this.countyDistrict = countyDistrict;
	}
	
	/**
	 * Convenience method to test whether any of the fields in this address are set
	 *
	 * @return whether any of the address fields (address1, address2, cityVillage, stateProvince,
	 *         country, countyDistrict, neighborhoodCell, postalCode, latitude, longitude, etc) are
	 *         whitespace, empty ("") or null.
	 */
	public boolean isBlank() {
		
		return StringUtils.isBlank(getAddress1()) && StringUtils.isBlank(getAddress2())
		        && StringUtils.isBlank(getAddress3()) && StringUtils.isBlank(getAddress4())
		        && StringUtils.isBlank(getAddress5()) && StringUtils.isBlank(getAddress6())
		        && StringUtils.isBlank(getCityVillage()) && StringUtils.isBlank(getStateProvince())
		        && StringUtils.isBlank(getCountry()) && StringUtils.isBlank(getCountyDistrict())
		        && StringUtils.isBlank(getPostalCode()) && StringUtils.isBlank(getLatitude())
		        && StringUtils.isBlank(getLongitude());
		
	}
			
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Note: this comparator imposes orderings that are inconsistent with equals.
	 */
	@Override
	@SuppressWarnings("squid:S1210")
	public int compareTo(PersonAddress other) {
		int retValue = 0;
		if (other != null) {
			retValue = getVoided().compareTo(other.getVoided());
			if (retValue == 0) {
				retValue = other.getPreferred().compareTo(getPreferred());
			}
			if (retValue == 0 && getDateCreated() != null) {
				retValue = OpenmrsUtil.compareWithNullAsLatest(getDateCreated(), other.getDateCreated());
			}
			if (retValue == 0) {
				retValue = OpenmrsUtil.compareWithNullAsGreatest(getPersonAddressId(), other.getPersonAddressId());
			}
			
			// if we've gotten this far, just check all address values. If they are
			// equal, leave the objects at 0. If not, arbitrarily pick retValue=1
			// and return that (they are not equal).
			if (retValue == 0 && !equalsContent(other)) {
				retValue = 1;
			}
		}
		return retValue;
	}
	
	/**
	 * @since 1.8
	 * @return the address3
	 */
	@Override
	public String getAddress3() {
		return address3;
	}
	
	/**
	 * @since 1.8
	 * @param address3 the address3 to set
	 */
	@Override
	public void setAddress3(String address3) {
		this.address3 = address3;
	}
	
	/**
	 * @since 1.8
	 * @return the address4
	 */
	@Override
	public String getAddress4() {
		return address4;
	}
	
	/**
	 * @since 1.8
	 * @param address4 the address4 to set
	 */
	@Override
	public void setAddress4(String address4) {
		this.address4 = address4;
	}
	
	/**
	 * @since 1.8
	 * @return the address6
	 */
	@Override
	public String getAddress6() {
		return address6;
	}
	
	/**
	 * @since 1.8
	 * @param address6 the address6 to set
	 */
	@Override
	public void setAddress6(String address6) {
		this.address6 = address6;
	}
	
	/**
	 * @since 1.8
	 * @return the address5
	 */
	@Override
	public String getAddress5() {
		return address5;
	}
	
	/**
	 * @since 1.8
	 * @param address5 the address5 to set
	 */
	@Override
	public void setAddress5(String address5) {
		this.address5 = address5;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		
		return getPersonAddressId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setPersonAddressId(id);
		
	}
	
	/**
	 * @return the startDate
	 * @since 1.9
	 */
	public Date getStartDate() {
		return startDate;
	}
	
	/**
	 * @param startDate to set to
	 * @since 1.9
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	/**
	 * @return the endDate
	 * @since 1.9
	 */
	public Date getEndDate() {
		return this.endDate;
	}
	
	/**
	 * @param endDate to set to
	 * @since 1.9
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	/**
	 * Returns true if the address' endDate is null
	 *
	 * @return true or false
	 * @since 1.9
	 */
	public Boolean isActive() {
		return this.endDate == null;
	}
	
	/**
	 * Makes an address inactive by setting its endDate to the current time
	 *
	 * @since 1.9
	 */
	public void deactivate() {
		setEndDate(Calendar.getInstance().getTime());
	}
	
	/**
	 * Makes an address active by setting its endDate to null
	 *
	 * @since 1.9
	 */
	public void activate() {
		setEndDate(null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAddress7() {
		return address7;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAddress7(String address7) {
		this.address7 = address7;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAddress8() {
		return address8;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAddress8(String address8) {
		this.address8 = address8;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAddress9() {
		return address9;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAddress9(String address9) {
		this.address9 = address9;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAddress10() {
		return address10;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAddress10(String address10) {
		this.address10 = address10;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAddress11() {
		return address11;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAddress11(String address11) {
		this.address11 = address11;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAddress12() {
		return address12;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAddress12(String address12) {
		this.address12 = address12;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAddress13() {
		return address13;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAddress13(String address13) {
		this.address13 = address13;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAddress14() {
		return address14;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAddress14(String address14) {
		this.address14 = address14;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAddress15() {
		return address15;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAddress15(String address15) {
		this.address15 = address15;
	}
}

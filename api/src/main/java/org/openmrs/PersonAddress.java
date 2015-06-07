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

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsUtil;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import static org.apache.commons.lang.StringUtils.defaultString;

/**
 * This class is the representation of a person's address. This class is many-to-one to the Person
 * class, so a Person/Patient/User can have zero to n addresses
 */
@Root(strict = false)
public class PersonAddress extends BaseOpenmrsData implements java.io.Serializable, Cloneable, Comparable<PersonAddress>, Address {
	
	public static final long serialVersionUID = 343333L;
	
	private static final Log log = LogFactory.getLog(PersonAddress.class);
	
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
	public String toString() {
		return new StringBuilder().append("a1:").append(getAddress1()).append(", a2:").append(getAddress2()).append(", cv:")
		        .append(getCityVillage()).append(", sp:").append(getStateProvince()).append(", c:").append(getCountry())
		        .append(", cd:").append(getCountyDistrict()).append(", nc:").append(getAddress3()).append(", pc:").append(
		            getPostalCode()).append(", lat:").append(getLatitude()).append(", long:").append(getLongitude())
		        .toString();
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
	@SuppressWarnings("unchecked")
	public boolean equalsContent(PersonAddress otherAddress) {
		return new EqualsBuilder().append(defaultString(otherAddress.getAddress1()), defaultString(address1)).append(
		    defaultString(otherAddress.getAddress2()), defaultString(address2)).append(
		    defaultString(otherAddress.getAddress3()), defaultString(address3)).append(
		    defaultString(otherAddress.getAddress4()), defaultString(address4)).append(
		    defaultString(otherAddress.getAddress5()), defaultString(address5)).append(
		    defaultString(otherAddress.getAddress6()), defaultString(address6)).append(
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
	@Element(data = true, required = false)
	public String getAddress1() {
		return address1;
	}
	
	/**
	 * @param address1 The address1 to set.
	 */
	@Element(data = true, required = false)
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	
	/**
	 * @return Returns the address2.
	 */
	@Element(data = true, required = false)
	public String getAddress2() {
		return address2;
	}
	
	/**
	 * @param address2 The address2 to set.
	 */
	@Element(data = true, required = false)
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	
	/**
	 * @return Returns the cityVillage.
	 */
	@Element(data = true, required = false)
	public String getCityVillage() {
		return cityVillage;
	}
	
	/**
	 * @param cityVillage The cityVillage to set.
	 */
	@Element(data = true, required = false)
	public void setCityVillage(String cityVillage) {
		this.cityVillage = cityVillage;
	}
	
	/**
	 * @return Returns the country.
	 */
	@Element(data = true, required = false)
	public String getCountry() {
		return country;
	}
	
	/**
	 * @param country The country to set.
	 */
	@Element(data = true, required = false)
	public void setCountry(String country) {
		this.country = country;
	}
	
	/**
	 * @return Returns the preferred.
	 */
	public Boolean isPreferred() {
		if (preferred == null) {
			return new Boolean(false);
		}
		return preferred;
	}
	
	@Attribute(required = true)
	public Boolean getPreferred() {
		return isPreferred();
	}
	
	/**
	 * @param preferred The preferred to set.
	 */
	@Attribute(required = true)
	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}
	
	/**
	 * @return Returns the latitude.
	 */
	@Attribute(required = false)
	public String getLatitude() {
		return latitude;
	}
	
	/**
	 * @param latitude The latitude to set.
	 */
	@Attribute(required = false)
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	/**
	 * @return Returns the longitude.
	 */
	@Attribute(required = false)
	public String getLongitude() {
		return longitude;
	}
	
	/**
	 * @param longitude The longitude to set.
	 */
	@Attribute(required = false)
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	/**
	 * @return Returns the person.
	 */
	@Element(required = true)
	public Person getPerson() {
		return person;
	}
	
	/**
	 * @param person The person to set.
	 */
	@Element(required = true)
	public void setPerson(Person person) {
		this.person = person;
	}
	
	/**
	 * @return Returns the personAddressId.
	 */
	@Attribute(required = true)
	public Integer getPersonAddressId() {
		return personAddressId;
	}
	
	/**
	 * @param personAddressId The personAddressId to set.
	 */
	@Attribute(required = true)
	public void setPersonAddressId(Integer personAddressId) {
		this.personAddressId = personAddressId;
	}
	
	/**
	 * @return Returns the postalCode.
	 */
	@Element(data = true, required = false)
	public String getPostalCode() {
		return postalCode;
	}
	
	/**
	 * @param postalCode The postalCode to set.
	 */
	@Element(data = true, required = false)
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	
	/**
	 * @return Returns the stateProvince.
	 */
	@Element(data = true, required = false)
	public String getStateProvince() {
		return stateProvince;
	}
	
	/**
	 * @param stateProvince The stateProvince to set.
	 */
	@Element(data = true, required = false)
	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}
	
	/**
	 * @return Returns the countyDistrict.
	 */
	@Element(data = true, required = false)
	public String getCountyDistrict() {
		return countyDistrict;
	}
	
	/**
	 * @param countyDistrict The countyDistrict to set.
	 */
	@Element(data = true, required = false)
	public void setCountyDistrict(String countyDistrict) {
		this.countyDistrict = countyDistrict;
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #getAddress3()}
	 * @return Returns the neighborhoodCell.
	 */
	@Deprecated
	@Element(data = true, required = false)
	public String getNeighborhoodCell() {
		return getAddress3();
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #setAddress3(String)}
	 * @param address3 The neighborhoodCell to set.
	 */
	@Deprecated
	@Element(data = true, required = false)
	public void setNeighborhoodCell(String address3) {
		this.setAddress3(address3);
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
	 * @deprecated As of 1.8, replaced by {@link #getAddress6()}
	 * @return the region
	 */
	@Deprecated
	@Element(data = true, required = false)
	public String getRegion() {
		return getAddress6();
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #setAddress6(String)}
	 * @param address6 the region to set
	 */
	@Deprecated
	@Element(data = true, required = false)
	public void setRegion(String address6) {
		this.setAddress6(address6);
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #getAddress5()}
	 * @return the subregion
	 */
	@Deprecated
	@Element(data = true, required = false)
	public String getSubregion() {
		return getAddress5();
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #setAddress5(String)}
	 * @param address5 the subregion to set
	 */
	@Deprecated
	@Element(data = true, required = false)
	public void setSubregion(String address5) {
		this.setAddress5(address5);
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #getAddress4()}
	 * @return the townshipDivision
	 */
	@Deprecated
	@Element(data = true, required = false)
	public String getTownshipDivision() {
		return getAddress4();
	}
	
	/**
	 * @deprecated As of 1.8, replaced by {@link #setAddress4(String)}
	 * @param address4 the address4 to set
	 */
	@Deprecated
	@Element(data = true, required = false)
	public void setTownshipDivision(String address4) {
		this.setAddress4(address4);
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Note: this comparator imposes orderings that are inconsistent with equals.
	 */
	@SuppressWarnings("squid:S1210")
	public int compareTo(PersonAddress other) {
		int retValue = 0;
		if (other != null) {
			retValue = isVoided().compareTo(other.isVoided());
			if (retValue == 0) {
				retValue = other.isPreferred().compareTo(isPreferred());
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
	public String getAddress3() {
		return address3;
	}
	
	/**
	 * @since 1.8
	 * @param address3 the address3 to set
	 */
	public void setAddress3(String address3) {
		this.address3 = address3;
	}
	
	/**
	 * @since 1.8
	 * @return the address4
	 */
	public String getAddress4() {
		return address4;
	}
	
	/**
	 * @since 1.8
	 * @param address4 the address4 to set
	 */
	public void setAddress4(String address4) {
		this.address4 = address4;
	}
	
	/**
	 * @since 1.8
	 * @return the address6
	 */
	public String getAddress6() {
		return address6;
	}
	
	/**
	 * @since 1.8
	 * @param address6 the address6 to set
	 */
	public void setAddress6(String address6) {
		this.address6 = address6;
	}
	
	/**
	 * @since 1.8
	 * @return the address5
	 */
	public String getAddress5() {
		return address5;
	}
	
	/**
	 * @since 1.8
	 * @param address5 the address5 to set
	 */
	public void setAddress5(String address5) {
		this.address5 = address5;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getPersonAddressId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
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
}

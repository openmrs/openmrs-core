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


import static org.apache.commons.lang.StringUtils.defaultString;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsUtil;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

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
	
	private String cityVillage;
	
	private String neighborhoodCell;
	
	private String countyDistrict;
	
	private String townshipDivision;
	
	private String region;
	
	private String subregion;
	
	private String stateProvince;
	
	private String country;
	
	private String postalCode;
	
	private String latitude;
	
	private String longitude;
	
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
		return new StringBuilder().append("a1:").append(getAddress1()).append(", a2:").append(getAddress2()).append(", cv:")
		        .append(getCityVillage()).append(", sp:").append(getStateProvince()).append(", c:").append(getCountry())
		        .append(", cd:").append(getCountyDistrict()).append(", nc:").append(getNeighborhoodCell()).append(", pc:")
		        .append(getPostalCode()).append(", lat:").append(getLatitude()).append(", long:").append(getLongitude())
		        .toString();
	}
	
	/**
	 * Compares this address to the given object/address for similarity. Uses the very basic
	 * comparison of just the PersonAddress.personAddressId
	 * 
	 * @param obj Object (Usually PersonAddress) with which to compare
	 * @return boolean true/false whether or not they are the same objects
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PersonAddress) {
			PersonAddress p = (PersonAddress) obj;
			if (this.getPersonAddressId() != null && p.getPersonAddressId() != null)
				return (this.getPersonAddressId().equals(p.getPersonAddressId()));
		}
		return false;
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
		return new EqualsBuilder().append(defaultString(otherAddress.getAddress1()), defaultString(address1))
		        .append(defaultString(otherAddress.getAddress2()), defaultString(address2))
		        .append(defaultString(otherAddress.getNeighborhoodCell()), defaultString(neighborhoodCell))
		        .append(defaultString(otherAddress.getTownshipDivision()), defaultString(townshipDivision))
		        .append(defaultString(otherAddress.getSubregion()), defaultString(subregion))
		        .append(defaultString(otherAddress.getRegion()), defaultString(region))
		        .append(defaultString(otherAddress.getCityVillage()), defaultString(cityVillage))
		        .append(defaultString(otherAddress.getCountyDistrict()), defaultString(countyDistrict))
		        .append(defaultString(otherAddress.getStateProvince()), defaultString(stateProvince))
		        .append(defaultString(otherAddress.getCountry()), defaultString(country))
		        .append(defaultString(otherAddress.getPostalCode()), defaultString(postalCode))
		        .append(defaultString(otherAddress.getLatitude()), defaultString(latitude))
		        .append(defaultString(otherAddress.getLongitude()), defaultString(longitude)).isEquals();
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.getPersonAddressId() == null)
			return super.hashCode();
		return this.getPersonAddressId().hashCode();
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
		if (preferred == null)
			return new Boolean(false);
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
	 * @return Returns the neighborhoodCell.
	 */
	@Element(data = true, required = false)
	public String getNeighborhoodCell() {
		return neighborhoodCell;
	}
	
	/**
	 * @param neighborhoodCell The neighborhoodCell to set.
	 */
	@Element(data = true, required = false)
	public void setNeighborhoodCell(String neighborhoodCell) {
		this.neighborhoodCell = neighborhoodCell;
	}
	
	/**
	 * Convenience method to test whether any of the fields in this address are set
	 * 
	 * @return whether any of the address fields (address1, address2, cityVillage, stateProvince,
	 *         country, countyDistrict, neighborhoodCell, postalCode, latitude, longitude) are
	 *         non-null
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
	 * @return the region
	 */
	@Element(data = true, required = false)
	public String getRegion() {
		return region;
	}
	
	/**
	 * @param region the region to set
	 */
	@Element(data = true, required = false)
	public void setRegion(String region) {
		this.region = region;
	}
	
	/**
	 * @return the subregion
	 */
	@Element(data = true, required = false)
	public String getSubregion() {
		return subregion;
	}
	
	/**
	 * @param subregion the subregion to set
	 */
	@Element(data = true, required = false)
	public void setSubregion(String subregion) {
		this.subregion = subregion;
	}
	
	/**
	 * @return the townshipDivision
	 */
	@Element(data = true, required = false)
	public String getTownshipDivision() {
		return townshipDivision;
	}
	
	/**
	 * @param townshipDivision the townshipDivision to set
	 */
	@Element(data = true, required = false)
	public void setTownshipDivision(String townshipDivision) {
		this.townshipDivision = townshipDivision;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(PersonAddress other) {
		int retValue = 0;
		if (other != null) {
			retValue = isVoided().compareTo(other.isVoided());
			if (retValue == 0)
				retValue = other.isPreferred().compareTo(isPreferred());
			if (retValue == 0 && getDateCreated() != null)
				retValue = OpenmrsUtil.compareWithNullAsLatest(getDateCreated(), other.getDateCreated());
			if (retValue == 0)
				retValue = OpenmrsUtil.compareWithNullAsGreatest(getPersonAddressId(), other.getPersonAddressId());
			
			// if we've gotten this far, just check all address values. If they are
			// equal, leave the objects at 0. If not, arbitrarily pick retValue=1
			// and return that (they are not equal).
			if (retValue == 0 && !equalsContent(other))
				retValue = 1;
		}
		return retValue;
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
	 * Added for compatibility reasons with 1.8+
	 */
	public String getAddress3() {
		return neighborhoodCell;
	}
	
	/**
	 * Added for compatibility reasons with 1.8+
	 */
	public void setAddress3(String address3) {
		this.neighborhoodCell = address3;
	}
	
	/**
	 * Added for compatibility reasons with 1.8+
	 */
	public String getAddress4() {
		return townshipDivision;
	}
	
	/**
	 * Added for compatibility reasons with 1.8+
	 */
	public void setAddress4(String address4) {
		this.townshipDivision = address4;
	}
	
	/**
	 * Added for compatibility reasons with 1.8+
	 */
	public String getAddress5() {
		return subregion;
	}
	
	/**
	 * Added for compatibility reasons with 1.8+
	 */
	public void setAddress5(String address5) {
		this.subregion = address5;
	}
	
	/**
	 * Added for compatibility reasons with 1.8+
	 */
	public void setAddress6(String address6) {
		this.region = address6;
	}
	
	/**
	 * Added for compatibility reasons with 1.8+
	 */
	public String getAddress6() {
		return region;
	}
}

package org.openmrs;

import java.util.Date;

/**
 * PersonAddress 
 * 
 * @author Ben Wolfe
 * @version 2.0
 */
public class PersonAddress implements java.io.Serializable, Cloneable {

	public static final long serialVersionUID = 343333L;

	// Fields

	private Integer personAddressId;
	
	private Person person;
	private Boolean preferred = false;

	private String address1;
	private String address2;
	private String cityVillage;
	private String neighborhoodCell;
	private String countyDistrict;
	private String stateProvince;
	private String country;
	private String postalCode;
	
	private String latitude;
	private String longitude;
	
	private User creator;
	private Date dateCreated;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;
	
	// Constructors

	/** default constructor */
	public PersonAddress() {
	}

	/** constructor with id */
	public PersonAddress(Integer personAddressId) {
		this.personAddressId = personAddressId;
	}

	public String toString() {
		return "a1:" + getAddress1() + ", a2:" + getAddress2() + ", cv:" +
			getCityVillage() + ", sp:" + getStateProvince() + ", c:" +
			getCountry() + ", cd:" + getCountyDistrict() + ", nc:" +
			getNeighborhoodCell() + ", pc:" + getPostalCode() + ", lat:" +
			getLatitude() + ", long:" + getLongitude();   
	}
	
	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PersonAddress) {
			PersonAddress p = (PersonAddress)obj;
			if (this.getPersonAddressId() != null && p.getPersonAddressId() != null)
				return (this.getPersonAddressId().equals(p.getPersonAddressId()));
			/*return (this.getAddress1().matches(p.getAddress1()) &&
					this.getAddress2().matches(p.getAddress2()) &&
					this.getCityVillage().matches(p.getCityVillage()) &&
					this.getStateProvince().equals(p.getStateProvince()) &&
					this.getCountry().matches(p.getCountry()));*/
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getPersonAddressId() == null) return super.hashCode();
		return this.getPersonAddressId().hashCode();
	}
	
	/**
	 * bitwise copy of the personAddress object.  
	 * NOTICE: THIS WILL NOT COPY THE PATIENT OBJECT.  The PersonAddress.person object in
	 * this object AND the cloned object will point at the same person
	 * @return New PersonAddress object
	 */
	public Object clone() {
		try {
	    	return super.clone(); 
		} catch (CloneNotSupportedException e) {
			throw new InternalError("PersonAddress should be cloneable");
		}
	}
	
	/**
	 * @return Returns the address1.
	 */
	public String getAddress1() {
		return address1;
	}

	/**
	 * @param address1 The address1 to set.
	 */
	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	/**
	 * @return Returns the address2.
	 */
	public String getAddress2() {
		return address2;
	}

	/**
	 * @param address2 The address2 to set.
	 */
	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	/**
	 * @return Returns the cityVillage.
	 */
	public String getCityVillage() {
		return cityVillage;
	}

	/**
	 * @param cityVillage The cityVillage to set.
	 */
	public void setCityVillage(String cityVillage) {
		this.cityVillage = cityVillage;
	}

	/**
	 * @return Returns the country.
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country The country to set.
	 */
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
	
	public Boolean getPreferred() {
		return isPreferred();
	}

	/**
	 * @param preferred The preferred to set.
	 */
	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the latitude.
	 */
	public String getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude The latitude to set.
	 */
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return Returns the longitude.
	 */
	public String getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude The longitude to set.
	 */
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
	public String getPostalCode() {
		return postalCode;
	}

	/**
	 * @param postalCode The postalCode to set.
	 */
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	/**
	 * @return Returns the stateProvince.
	 */
	public String getStateProvince() {
		return stateProvince;
	}

	/**
	 * @param stateProvince The stateProvince to set.
	 */
	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}

	/**
	 * @return Returns the dateVoided.
	 */
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * @param dateVoided The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * @return Returns the voided.
	 */
	public Boolean isVoided() {
		return voided;
	}
	
	public Boolean getVoided() {
		return isVoided();
	}

	/**
	 * @param voided The voided to set.
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * @return Returns the voidedBy.
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy The voidedBy to set.
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return Returns the voidReason.
	 */
	public String getVoidReason() {
		return voidReason;
	}

	/**
	 * @param voidReason The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	/**
	 * @return Returns the countyDistrict.
	 */
	public String getCountyDistrict() {
		return countyDistrict;
	}

	/**
	 * @param countyDistrict The countyDistrict to set.
	 */
	public void setCountyDistrict(String countyDistrict) {
		this.countyDistrict = countyDistrict;
	}

	/**
	 * @return Returns the neighborhoodCell.
	 */
	public String getNeighborhoodCell() {
		return neighborhoodCell;
	}

	/**
	 * @param neighborhoodCell The neighborhoodCell to set.
	 */
	public void setNeighborhoodCell(String neighborhoodCell) {
		this.neighborhoodCell = neighborhoodCell;
	}

	/**
	 * Convenience method to test whether any of the fields in this address are set
	 * @return whether any of the address fields (address1, address2, cityVillage, stateProvince, country, countyDistrict, neighborhoodCell, postalCode, latitute, logitude) are non-null
	 */
	public boolean isBlank() {
		return getAddress1() == null && getAddress2() == null &&
			getCityVillage() == null && getStateProvince() == null &&
			getCountry() == null && getCountyDistrict() == null && 
			getNeighborhoodCell() == null && getPostalCode() == null && 
			getLatitude() == null && getLongitude() == null ;
	}
}
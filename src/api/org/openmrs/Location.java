package org.openmrs;

import java.util.Date;
import java.util.Set;

/**
 * Location 
 */
public class Location implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer locationId;
	private String name;
	private String description;
	private String address1;
	private String address2;
	private String cityVillage;
	private String stateProvince;
	private String country;
	private String zipcode;
	private String latitude;
	private String longitude;
	private User creator;
	private Date dateCreated;

	// Constructors

	/** default constructor */
	public Location() {
	}

	/** constructor with id */
	public Location(Integer locationId) {
		this.locationId = locationId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getLocationId() {
		return this.locationId;
	}

	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}

	/**
	 * 
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 */
	public String getAddress1() {
		return this.address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	/**
	 * 
	 */
	public String getAddress2() {
		return this.address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	/**
	 * 
	 */
	public String getCityVillage() {
		return this.cityVillage;
	}

	public void setCityVillage(String cityVillage) {
		this.cityVillage = cityVillage;
	}

	/**
	 * 
	 */
	public String getStateProvince() {
		return this.stateProvince;
	}

	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}

	/**
	 * 
	 */
	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * 
	 */
	public String getZipcode() {
		return this.zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	/**
	 * 
	 */
	public String getLatitude() {
		return this.latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	/**
	 * 
	 */
	public String getLongitude() {
		return this.longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	/**
	 * 
	 */
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * 
	 */
	public User getCreator() {
		return this.creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

}
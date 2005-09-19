package org.openmrs;

import java.util.Date;

/**
 * PatientAddress 
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class PatientAddress implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer patientAddressId;
	private String address1;
	private String address2;
	private String cityVillage;
	private String stateProvince;
	private String country;
	private String postalCode;
	private String latitude;
	private String longitude;
	private Date dateCreated;
	private Patient patient;
	private User creator;
	private boolean dirty;

	// Constructors

	/** default constructor */
	public PatientAddress() {
	}

	/** constructor with id */
	public PatientAddress(Integer patientAddressId) {
		this.patientAddressId = patientAddressId;
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PatientAddress) {
			PatientAddress p = (PatientAddress)obj;
			if (this.getPatientAddressId() != null && p.getPatientAddressId() != null)
				return (this.getPatientAddressId().equals(p.getPatientAddressId()));
			/*return (this.getAddress1().matches(p.getAddress1()) &&
					this.getAddress2().matches(p.getAddress2()) &&
					this.getCityVillage().matches(p.getCityVillage()) &&
					this.getStateProvince().equals(p.getStateProvince()) &&
					this.getCountry().matches(p.getCountry()));*/
		}
		return false;
	}
	
	/**
	 * Returns whether or not this address has been modified
	 * 
	 * @return true/false whether this has been modified
	 */
	public boolean isDirty() {
		return dirty;
	}
	
	// Property accessors

	/**
	 * Unset the dirty bit after modification
	 */
	public void setClean() {
		dirty = false;
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
		dirty = true;
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
		dirty = true;
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
		dirty = true;
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
		dirty = true;
		this.country = country;
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
		dirty = true;
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
		dirty = true;
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
		dirty = true;
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
		dirty = true;
		this.longitude = longitude;
	}

	/**
	 * @return Returns the patient.
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param patient The patient to set.
	 */
	public void setPatient(Patient patient) {
		dirty = true;
		this.patient = patient;
	}

	/**
	 * @return Returns the patientAddressId.
	 */
	public Integer getPatientAddressId() {
		return patientAddressId;
	}

	/**
	 * @param patientAddressId The patientAddressId to set.
	 */
	public void setPatientAddressId(Integer patientAddressId) {
		dirty = true;
		this.patientAddressId = patientAddressId;
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
		dirty = true;
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
		dirty = true;
		this.stateProvince = stateProvince;
	}


}
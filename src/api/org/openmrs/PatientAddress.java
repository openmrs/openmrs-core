package org.openmrs;

import java.util.Date;

/**
 * PatientAddress 
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class PatientAddress implements java.io.Serializable, Cloneable {

	public static final long serialVersionUID = 343333L;

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
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;
	private Boolean preferred = false;

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
	
	public int hashCode() {
		if (this.getPatientAddressId() == null) return super.hashCode();
		return this.getPatientAddressId().hashCode();
	}
	
	/**
	 * bitwise copy of the patientAddress object.  
	 * NOTICE: THIS WILL NOT COPY THE PATIENT OBJECT.  The PatientAddress.patient object in
	 * this object AND the cloned object will point at the same patient
	 * @return New PatientAddress object
	 */
	public Object clone() {
		try {
	    	return super.clone(); 
		} catch (CloneNotSupportedException e) {
			throw new InternalError("PatientAddress should be cloneable");
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
	 * @return Returns the patient.
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param patient The patient to set.
	 */
	public void setPatient(Patient patient) {
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
}
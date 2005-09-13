package org.openmrs;

import java.util.Date;

/**
 * PatientName
 */
public class PatientName implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer patientNameId;
	private Patient patient;
	private Boolean preferred;
	private String prefix;
	private String givenName;
	private String middleName;
	private String familyNamePrefix;
	private String familyName;
	private String familyName2;
	private String familyNameSuffix;
	private String degree;
	private Date dateCreated;
	private User creator;
	private boolean voided;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;

	// Constructors

	/** default constructor */
	public PatientName() {
	}

	/** constructor with id */
	public PatientName(Integer patientNameId) {
		this.patientNameId = patientNameId;
	}

	public boolean equals(Object obj) {
		if (obj instanceof PatientName) {
			PatientName pname = (PatientName) obj;
			if (this.getPatientNameId() != null && pname.getPatientNameId() != null)
				return (this.getPatientNameId() == pname.getPatientNameId()); 
		}
		return false;
	}
	
	// Property accessors

	/**
	 * 
	 */
	public Integer getPatientNameId() {
		return this.patientNameId;
	}

	public void setPatientNameId(Integer patientNameId) {
		this.patientNameId = patientNameId;
	}

	/**
	 * 
	 */
	public Boolean getPreferred() {
		return this.preferred;
	}

	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}

	/**
	 * 
	 */
	public String getGivenName() {
		return this.givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	/**
	 * 
	 */
	public String getMiddleName() {
		return this.middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * 
	 */
	public String getFamilyNamePrefix() {
		return this.familyNamePrefix;
	}

	public void setFamilyNamePrefix(String familyNamePrefix) {
		this.familyNamePrefix = familyNamePrefix;
	}

	/**
	 * 
	 */
	public String getFamilyName() {
		return this.familyName;
	}

	public void setFamilyName(String famiyName) {
		this.familyName = famiyName;
	}

	/**
	 * 
	 */
	public String getFamilyName2() {
		return this.familyName2;
	}

	public void setFamilyName2(String familyName2) {
		this.familyName2 = familyName2;
	}

	/**
	 * 
	 */
	public String getFamilyNameSuffix() {
		return this.familyNameSuffix;
	}

	public void setFamilyNameSuffix(String familyNameSuffix) {
		this.familyNameSuffix = familyNameSuffix;
	}

	/**
	 * 
	 */
	public String getDegree() {
		return this.degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
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
	public Patient getPatient() {
		return this.patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
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

	public String toString() {
		return givenName + " " + familyName;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setVoided(boolean voided) {
		this.voided = voided;
	}

	public boolean isVoided() {
		return voided;
	}

	public User getVoidedBy() {
		return voidedBy;
	}

	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	public Date getDateVoided() {
		return dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	public String getVoidReason() {
		return voidReason;
	}

	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

}
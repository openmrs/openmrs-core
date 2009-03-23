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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.util.OpenmrsUtil;

/**
 * Defines a Patient in the system. A patient is simply an extension of a person and all that that
 * implies.
 * 
 * @version 2.0
 */
public class Patient extends Person implements java.io.Serializable {
	
	public static final long serialVersionUID = 93123L;
	
	protected static final Log log = LogFactory.getLog(Patient.class);
	
	// Fields
	
	//private Person person;
	
	private Integer patientId;
	
	private Set<PatientIdentifier> identifiers;
	
	private User creator;
	
	private Date dateCreated;
	
	private User changedBy;
	
	private Date dateChanged;
	
	private Boolean voided = false;
	
	private User voidedBy;
	
	private Date dateVoided;
	
	private String voidReason;
	
	// Constructors
	/** default constructor */
	public Patient() {
	}
	
	public Patient(Person person) {
		super(person);
		if (person != null)
			this.patientId = person.getPersonId();
	}
	
	/**
	 * Constructor with default patient id
	 * 
	 * @param patientId
	 */
	public Patient(Integer patientId) {
		super(patientId);
		this.patientId = patientId;
	}
	
	/**
	 * Compares two objects for similarity This must pass through to the parent object
	 * (org.openmrs.Person) in order to get similarity of person/patient objects
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 * @see org.openmrs.Person#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	/**
	 * The hashcode for a patient/person is used to index the objects in a tree This must pass
	 * through to the parent object (org.openmrs.Person) in order to get similarity of
	 * person/patient objects
	 * 
	 * @see org.openmrs.Person#hashCode()
	 */
	public int hashCode() {
		return super.hashCode();
	}
	
	// Property accessors
	
	/**
	 * @return internal identifier for patient
	 */
	public Integer getPatientId() {
		return this.patientId;
	}
	
	/**
	 * Sets the internal identifier for a patient. <b>This should never be called directly</b>. It
	 * exists only for the use of the supporting infrastructure.
	 * 
	 * @param patientId
	 */
	public void setPatientId(Integer patientId) {
		super.setPersonId(patientId);
		this.patientId = patientId;
	}
	
	/**
	 * Overrides the parent setPersonId(Integer) so that we can be sure patient id is also set
	 * correctly.
	 * 
	 * @see org.openmrs.Person#setPersonId(java.lang.Integer)
	 */
	public void setPersonId(Integer personId) {
		super.setPersonId(personId);
		this.patientId = personId;
	}
	
	/**
	 * @return patient's tribe
	 * @deprecated Tribe is not long a value on Patient. Install the Tribe module
	 */
	public Tribe getTribe() {
		throw new APIException("The Patient.getTribe method is no longer supported.  Install the Tribe module");
	}
	
	/**
	 * @param tribe patient's tribe
	 * @deprecated Tribe is not long a value on Patient. Install the Tribe module
	 */
	public void setTribe(Tribe tribe) {
		throw new APIException("The Patient.setTribe(Tribe) method is no longer supported.  Install the Tribe module");
	}
	
	/**
	 * Get all of this patients identifiers -- both voided and non-voided ones. If you want only
	 * non-voided identifiers, use {@link #getActiveIdentifiers()}
	 * 
	 * @return Set of all known identifiers for this patient
	 * @see org.openmrs.PatientIdentifier
	 * @see #getActiveIdentifiers()
	 */
	public Set<PatientIdentifier> getIdentifiers() {
		if (identifiers == null)
			identifiers = new TreeSet<PatientIdentifier>();
		return this.identifiers;
	}
	
	/**
	 * Update all identifiers for patient
	 * 
	 * @param identifiers Set<PatientIdentifier> to set as update all known identifiers for patient
	 * @see org.openmrs.PatientIdentifier
	 */
	public void setIdentifiers(Set<PatientIdentifier> identifiers) {
		this.identifiers = identifiers;
	}
	
	/**
	 * Adds this PatientIdentifier if the patient doesn't contain it already
	 * 
	 * @param patientIdentifier
	 */
	/**
	 * Will only add PatientIdentifiers in this list that this patient does not have already
	 * 
	 * @param patientIdentifiers
	 */
	public void addIdentifiers(Collection<PatientIdentifier> patientIdentifiers) {
		for (PatientIdentifier identifier : patientIdentifiers)
			addIdentifier(identifier);
	}
	
	/**
	 * Will add this PatientIdentifier if the patient doesn't contain it already
	 * 
	 * @param patientIdentifier
	 */
	public void addIdentifier(PatientIdentifier patientIdentifier) {
		patientIdentifier.setPatient(this);
		if (getIdentifiers() == null)
			identifiers = new TreeSet<PatientIdentifier>();
		if (patientIdentifier != null && !OpenmrsUtil.collectionContains(identifiers, patientIdentifier))
			identifiers.add(patientIdentifier);
	}
	
	/**
	 * Convenience method to remove the given identifier from this patient's list of identifiers. If
	 * <code>patientIdentifier</code> is null, nothing is done.
	 * 
	 * @param patientIdentifier the identifier to remove
	 */
	public void removeIdentifier(PatientIdentifier patientIdentifier) {
		if (getIdentifiers() != null && patientIdentifier != null)
			identifiers.remove(patientIdentifier);
	}
	
	/**
	 * Convenience method to get the first "preferred" identifier for a patient. Otherwise, returns
	 * the first non-voided identifier Otherwise, null
	 * 
	 * @return Returns the "preferred" patient identifier.
	 */
	public PatientIdentifier getPatientIdentifier() {
		if (getIdentifiers() != null && getIdentifiers().size() > 0) {
			for (PatientIdentifier id : getIdentifiers()) {
				if (id.isPreferred() && !id.isVoided())
					return id;
			}
			for (PatientIdentifier id : getIdentifiers()) {
				if (!id.isVoided())
					return id;
			}
			return null;
		}
		return null;
		
	}
	
	/**
	 * Returns the first (preferred) patient identifier matching a
	 * <code>PatientIdentifierType</code> Otherwise, returns the first non-voided identifier
	 * Otherwise, null
	 * 
	 * @param pit The PatientIdentifierType of which to return the PatientIdentifier
	 * @return Returns a PatientIdentifier of the specified type.
	 */
	public PatientIdentifier getPatientIdentifier(PatientIdentifierType pit) {
		if (getIdentifiers() != null && getIdentifiers().size() > 0) {
			for (PatientIdentifier id : getIdentifiers()) {
				if (id.isPreferred() && !id.isVoided() && pit.equals(id.getIdentifierType()))
					return id;
			}
			for (PatientIdentifier id : getIdentifiers()) {
				if (!id.isVoided() && pit.equals(id.getIdentifierType()))
					return id;
			}
			return null;
		}
		return null;
	}
	
	/**
	 * Return's the first (preferred) patient identifier matching <code>identifierTypeId</code>
	 * 
	 * @param identifierTypeId
	 * @return preferred patient identifier
	 */
	public PatientIdentifier getPatientIdentifier(Integer identifierTypeId) {
		if (getIdentifiers() != null && getIdentifiers().size() > 0) {
			for (PatientIdentifier id : getIdentifiers()) {
				if (id.isPreferred() && !id.isVoided()
				        && identifierTypeId.equals(id.getIdentifierType().getPatientIdentifierTypeId()))
					return id;
			}
			for (PatientIdentifier id : getIdentifiers()) {
				if (!id.isVoided() && identifierTypeId.equals(id.getIdentifierType().getPatientIdentifierTypeId()))
					return id;
			}
			return null;
		}
		return null;
	}
	
	/**
	 * Return's the (preferred) patient identifier matching <code>identifierTypeName</code>
	 * Otherwise returns that last <code>PatientIdenitifer</code>
	 * 
	 * @param identifierTypeName
	 * @return preferred patient identifier
	 */
	public PatientIdentifier getPatientIdentifier(String identifierTypeName) {
		if (getIdentifiers() != null && getIdentifiers().size() > 0) {
			for (PatientIdentifier id : getIdentifiers()) {
				if (id.isPreferred() && !id.isVoided() && identifierTypeName.equals(id.getIdentifierType().getName()))
					return id;
			}
			for (PatientIdentifier id : getIdentifiers()) {
				if (!id.isVoided() && identifierTypeName.equals(id.getIdentifierType().getName()))
					return id;
			}
			return null;
		}
		return null;
	}
	
	/**
	 * Returns only the non-voided identifiers for this patient. If you want <u>all</u> identifiers,
	 * use {@link #getIdentifiers()}
	 * 
	 * @return list of non-voided identifiers for this patient
	 * @see #getIdentifiers()
	 */
	public List<PatientIdentifier> getActiveIdentifiers() {
		List<PatientIdentifier> ids = new Vector<PatientIdentifier>();
		if (getIdentifiers() != null) {
			for (PatientIdentifier pi : getIdentifiers()) {
				if (pi.isVoided() == false)
					ids.add(pi);
			}
		}
		return ids;
	}
	
	/**
	 * Returns only the non-voided identifiers for this patient. If you want <u>all</u> identifiers,
	 * use {@link #getIdentifiers()}
	 * 
	 * @return list of non-voided identifiers for this patient
	 * @param pit PatientIdentifierType
	 * @see #getIdentifiers()
	 */
	public List<PatientIdentifier> getPatientIdentifiers(PatientIdentifierType pit) {
		List<PatientIdentifier> ids = new Vector<PatientIdentifier>();
		if (getIdentifiers() != null) {
			for (PatientIdentifier pi : getIdentifiers()) {
				if (pi.isVoided() == false && pit.equals(pi.getIdentifierType()))
					ids.add(pi);
			}
		}
		return ids;
	}
	
	public String toString() {
		return "Patient#" + patientId;
	}
	
	public User getChangedBy() {
		return changedBy;
	}
	
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	public User getCreator() {
		return creator;
	}
	
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	public Date getDateChanged() {
		return dateChanged;
	}
	
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public Date getDateVoided() {
		return dateVoided;
	}
	
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	
	public Boolean getVoided() {
		return isVoided();
	}
	
	public Boolean isVoided() {
		return voided;
	}
	
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	
	public User getVoidedBy() {
		return voidedBy;
	}
	
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}
	
	public String getVoidReason() {
		return voidReason;
	}
	
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
}

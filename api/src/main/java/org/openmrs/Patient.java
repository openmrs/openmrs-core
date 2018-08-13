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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.search.annotations.ContainedIn;

/**
 * Defines a Patient in the system. A patient is simply an extension of a person and all that that
 * implies.
 * 
 * @version 2.0
 */
public class Patient extends Person {
	
	public static final long serialVersionUID = 93123L;

	private Integer patientId;

	private String allergyStatus = Allergies.UNKNOWN;

	@ContainedIn
	private Set<PatientIdentifier> identifiers;
	
	// Constructors
	
	/** default constructor */
	public Patient() {
		setPatient(true);
	}
	
	/**
	 * This constructor creates a new Patient object from the given {@link Person} object. All
	 * attributes are copied over to the new object. NOTE! All child collection objects are copied
	 * as pointers, each individual element is not copied. <br>
	 * <br>
	 *
	 * @param person the person object to copy onto a new Patient
	 * @see Person#Person(Person)
	 */
	public Patient(Person person) {
		super(person);
		if (person != null) {
			this.patientId = person.getPersonId();
			if (person.getUuid() != null) {
				this.setUuid(person.getUuid());
			}
		}
		setPatient(true);
	}
	
	/**
	 * Constructor with default patient id
	 * 
	 * @param patientId
	 */
	public Patient(Integer patientId) {
		super(patientId);
		this.patientId = patientId;
		setPatient(true);
	}

	/**
	 * This constructor creates a new Patient object from the given {@link Patient} object. All
	 * attributes are copied over to the new object. In effect creating a clone/duplicate.
	 * <br>
	 *
	 * @param patient the person object to copy onto a new Patient
	 * @since 2.2.0
	 */
	public Patient(Patient patient){
		super(patient);
		this.patientId = patient.getPatientId();
		this.allergyStatus = patient.getAllergyStatus();
		Set<PatientIdentifier> newIdentifiers = new TreeSet<>();
		for (PatientIdentifier pid : patient.getIdentifiers()) {
			PatientIdentifier identifierClone = (PatientIdentifier) pid.clone();
			identifierClone.setPatient(this);
			newIdentifiers.add(identifierClone);
		}
		this.identifiers = newIdentifiers;
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
	 * Returns allergy status maintained by the supporting infrastructure.
	 * 
	 * @return current allargy status for patient
	 * @since 2.0
	 * @should return allergy status maintained by the supporting infrastructure
	 */
	public String getAllergyStatus() {
		return this.allergyStatus;
	}
	
	/**
	 * Sets the allergy status for a patient. <b>This should never be called directly</b>. 
	 * It should reflect allergy status maintained by the supporting infrastructure.
	 * 
	 * @param allergyStatus
	 * @since 2.0
	 * @should not be called by service client
	 */
	public void setAllergyStatus(String allergyStatus) {
		this.allergyStatus = allergyStatus;
	}
	
	/**
	 * Overrides the parent setPersonId(Integer) so that we can be sure patient id is also set
	 * correctly.
	 * 
	 * @see org.openmrs.Person#setPersonId(java.lang.Integer)
	 */
	@Override
	public void setPersonId(Integer personId) {
		super.setPersonId(personId);
		this.patientId = personId;
	}

	/**
	 * Get all of this patients identifiers -- both voided and non-voided ones. If you want only
	 * non-voided identifiers, use {@link #getActiveIdentifiers()}
	 * 
	 * @return Set of all known identifiers for this patient
	 * @see org.openmrs.PatientIdentifier
	 * @see #getActiveIdentifiers()
	 * @should not return null
	 */
	public Set<PatientIdentifier> getIdentifiers() {
		if (identifiers == null) {
			identifiers = new TreeSet<>();
		}
		return this.identifiers;
	}
	
	/**
	 * Update all identifiers for patient
	 * 
	 * @param identifiers Set&lt;PatientIdentifier&gt; to set as update all known identifiers for patient
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
		for (PatientIdentifier identifier : patientIdentifiers) {
			addIdentifier(identifier);
		}
	}
	
	/**
	 * Will add this PatientIdentifier if the patient doesn't contain it already
	 * 
	 * @param patientIdentifier
	 * @should not fail with null identifiers list
	 * @should add identifier to current list
	 * @should not add identifier that is in list already
	 */
	public void addIdentifier(PatientIdentifier patientIdentifier) {
		if (patientIdentifier != null) {
			patientIdentifier.setPatient(this);
			// make sure the set doesn't already contain an identifier with the same
			// identifier, identifierType
			for (PatientIdentifier currentId : getActiveIdentifiers()) {
				if (currentId.equalsContent(patientIdentifier)) {
					return; // fail silently if someone tries to add a duplicate
				}
			}
		}
		
		getIdentifiers().add(patientIdentifier);
	}
	
	/**
	 * Convenience method to remove the given identifier from this patient's list of identifiers. If
	 * <code>patientIdentifier</code> is null, nothing is done.
	 * 
	 * @param patientIdentifier the identifier to remove
	 * @should remove identifier if exists
	 */
	public void removeIdentifier(PatientIdentifier patientIdentifier) {
		if (patientIdentifier != null) {
			getIdentifiers().remove(patientIdentifier);
		}
	}
	
	/**
	 * Convenience method to get the first "preferred" identifier for a patient. Otherwise, returns
	 * the first non-voided identifier Otherwise, null
	 * 
	 * @return Returns the "preferred" patient identifier.
	 */
	public PatientIdentifier getPatientIdentifier() {
		// normally the DAO layer returns these in the correct order, i.e. preferred and non-voided first, but it's possible that someone
		// has fetched a Patient, changed their identifiers around, and then calls this method, so we have to be careful.
		if (!getIdentifiers().isEmpty()) {
			for (PatientIdentifier id : getIdentifiers()) {
				if (id.getPreferred() && !id.getVoided()) {
					return id;
				}
			}
			for (PatientIdentifier id : getIdentifiers()) {
				if (!id.getVoided()) {
					return id;
				}
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
		if (!getIdentifiers().isEmpty()) {
			for (PatientIdentifier id : getIdentifiers()) {
				if (id.getPreferred() && !id.getVoided() && pit.equals(id.getIdentifierType())) {
					return id;
				}
			}
			for (PatientIdentifier id : getIdentifiers()) {
				if (!id.getVoided() && pit.equals(id.getIdentifierType())) {
					return id;
				}
			}
			return null;
		}
		return null;
	}
	
	/**
	 * Returns the first (preferred) patient identifier matching <code>identifierTypeId</code>
	 * 
	 * @param identifierTypeId
	 * @return preferred patient identifier
	 */
	public PatientIdentifier getPatientIdentifier(Integer identifierTypeId) {
		if (!getIdentifiers().isEmpty()) {
			for (PatientIdentifier id : getIdentifiers()) {
				if (id.getPreferred() && !id.getVoided()
				        && identifierTypeId.equals(id.getIdentifierType().getPatientIdentifierTypeId())) {
					return id;
				}
			}
			for (PatientIdentifier id : getIdentifiers()) {
				if (!id.getVoided() && identifierTypeId.equals(id.getIdentifierType().getPatientIdentifierTypeId())) {
					return id;
				}
			}
			return null;
		}
		return null;
	}
	
	/**
	 * Returns the (preferred) patient identifier matching <code>identifierTypeName</code> Otherwise
	 * returns that last <code>PatientIdenitifer</code>
	 * 
	 * @param identifierTypeName
	 * @return preferred patient identifier
	 */
	public PatientIdentifier getPatientIdentifier(String identifierTypeName) {
		if (!getIdentifiers().isEmpty()) {
			for (PatientIdentifier id : getIdentifiers()) {
				if (id.getPreferred() && !id.getVoided() && identifierTypeName.equals(id.getIdentifierType().getName())) {
					return id;
				}
			}
			for (PatientIdentifier id : getIdentifiers()) {
				if (!id.getVoided() && identifierTypeName.equals(id.getIdentifierType().getName())) {
					return id;
				}
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
	 * @should return preferred identifiers first in the list
	 */
	public List<PatientIdentifier> getActiveIdentifiers() {
		List<PatientIdentifier> ids = new ArrayList<>();
		List<PatientIdentifier> nonPreferred = new LinkedList<>();
		for (PatientIdentifier pi : getIdentifiers()) {
			if (!pi.getVoided()) {
				if (pi.getPreferred()) {
					ids.add(pi);
				} else {
					nonPreferred.add(pi);
				}
			}
		}
		ids.addAll(nonPreferred);
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
		List<PatientIdentifier> ids = new ArrayList<>();
		for (PatientIdentifier pi : getIdentifiers()) {
			if (!pi.getVoided() && pit.equals(pi.getIdentifierType())) {
				ids.add(pi);
			}
		}
		return ids;
	}
	
	@Override
	public String toString() {
		return "Patient#" + patientId;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getPatientId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setPatientId(id);
	}
	
	/**
	 * Returns the person represented
	 * 
	 * @return the person represented by this object
	 * @since 1.10.0
	 */
	public Person getPerson() {
		return this;
	}
}

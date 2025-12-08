/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.person;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.api.PatientService;

/**
 * This class is used for communicating to the <code>PatientService</code> the data that
 * needs to be serialized. This data represents the details of a merge. It is also used for
 * abstracting the serialization outside of the PatientService and to allow storing the
 * deserialized form of the merged data
 *
 * @see PersonMergeLog
 * @see PatientService#mergePatients(org.openmrs.Patient, org.openmrs.Patient)
 * @since 1.9
 */
public class PersonMergeLogData {
	
	/**
	 * List of UUIDs of visits moved from non-preferred to preferred
	 */
	private List<String> movedVisits;
	
	/**
	 * List of UUIDs of encounters moved from non-preferred to preferred
	 */
	private List<String> movedEncounters;
	
	/**
	 * List of UUIDs of patient programs copied from non-preferred to preferred
	 * (Deprecated in 2.6.8 and 2.7.0+, as we now move programs)
	 */
	@Deprecated
	private List<String> createdPrograms;

	/**
	 * List of UUIDs of patient programs moved from non-preferred to preferred
	 */
	private List<String> movedPrograms;
	
	/**
	 * List of UUIDs of voided relationships
	 */
	private List<String> voidedRelationships;
	
	/**
	 * List of UUIDs of created relationships
	 */
	private List<String> createdRelationships;
	
	/**
	 * List of UUIDs of observations not contained within any encounter moved from non-preferred to
	 * preferred
	 */
	private List<String> movedIndependentObservations;
	
	/**
	 * List of UUIDs of orders copied from non-preferred to preferred
	 */
	private List<String> createdOrders;
	
	/**
	 * List of UUIDs of identifiers copied from non-preferred to preferred
	 */
	private List<String> createdIdentifiers;
	
	/**
	 * List of UUIDs of addresses copied from non-preferred to preferred
	 */
	private List<String> createdAddresses;
	
	/**
	 * List of UUIDs of names copied from non-preferred to preferred
	 */
	private List<String> createdNames;
	
	/**
	 * List of UUIDs of attributes copied from non-preferred to preferred
	 */
	private List<String> createdAttributes;
	
	/**
	 * List of UUIDs of users moved to be associated from non-preferred to be associated to
	 * preferred
	 */
	private List<String> movedUsers;
	
	/**
	 * Value of gender of preferred person as it was before the merge occurred
	 */
	private String priorGender;
	
	/**
	 * Value of Date of Birth of preferred person as it was before the merge occurred
	 */
	private Date priorDateOfBirth;
	
	/**
	 * Whether the date of birth of preferred person was an estimated value before the merge
	 * occurred
	 */
	private boolean priorDateOfBirthEstimated;
	
	/**
	 * Value of Date of Death of preferred person as it was before the merge occurred
	 */
	private Date priorDateOfDeath;
	
	/**
	 * Whether the date of death of preferred person was an estimated value before the merge
	 * occurred
	 */
	private Boolean priorDateOfDeathEstimated;
	
	/**
	 * Value of cause of death of preferred person as it was before the merge occurred
	 */
	private String priorCauseOfDeath;
	
	public List<String> getMovedVisits() {
		return movedVisits;
	}
	
	public List<String> getMovedEncounters() {
		return movedEncounters;
	}
	
	public void addMovedVisit(String uuid) {
		if (movedVisits == null) {
			movedVisits = new ArrayList<>();
		}
		movedVisits.add(uuid);
	}
	
	public void addMovedEncounter(String uuid) {
		if (movedEncounters == null) {
			movedEncounters = new ArrayList<>();
		}
		movedEncounters.add(uuid);
	}
	
	@Deprecated
	public List<String> getCreatedPrograms() {
		return createdPrograms;
	}
	
	@Deprecated
	public void addCreatedProgram(String uuid) {
		if (createdPrograms == null) {
			createdPrograms = new ArrayList<>();
		}
		createdPrograms.add(uuid);
	}

	public List<String> getMovedPrograms() {
		return movedPrograms;
	}

	public void addMovedProgram(String uuid) {
		if (movedPrograms == null) {
			movedPrograms = new ArrayList<>();
		}
		movedPrograms.add(uuid);
	}
	
	public List<String> getVoidedRelationships() {
		return voidedRelationships;
	}
	
	public void addVoidedRelationship(String uuid) {
		if (voidedRelationships == null) {
			voidedRelationships = new ArrayList<>();
		}
		voidedRelationships.add(uuid);
	}
	
	public List<String> getCreatedRelationships() {
		return createdRelationships;
	}
	
	public void addCreatedRelationship(String uuid) {
		if (createdRelationships == null) {
			createdRelationships = new ArrayList<>();
		}
		createdRelationships.add(uuid);
	}
	
	public List<String> getMovedIndependentObservations() {
		return movedIndependentObservations;
	}
	
	public void addMovedIndependentObservation(String uuid) {
		if (movedIndependentObservations == null) {
			movedIndependentObservations = new ArrayList<>();
		}
		movedIndependentObservations.add(uuid);
	}
	
	public List<String> getCreatedOrders() {
		return createdOrders;
	}
	
	public void addCreatedOrder(String uuid) {
		if (createdOrders == null) {
			createdOrders = new ArrayList<>();
		}
		createdOrders.add(uuid);
	}
	
	public List<String> getCreatedIdentifiers() {
		return createdIdentifiers;
	}
	
	public void addCreatedIdentifier(String uuid) {
		if (createdIdentifiers == null) {
			createdIdentifiers = new ArrayList<>();
		}
		createdIdentifiers.add(uuid);
	}
	
	public List<String> getCreatedAddresses() {
		return createdAddresses;
	}
	
	public void addCreatedAddress(String uuid) {
		if (createdAddresses == null) {
			createdAddresses = new ArrayList<>();
		}
		createdAddresses.add(uuid);
	}
	
	public List<String> getCreatedNames() {
		return createdNames;
	}
	
	public void addCreatedName(String uuid) {
		if (createdNames == null) {
			createdNames = new ArrayList<>();
		}
		createdNames.add(uuid);
	}
	
	public List<String> getCreatedAttributes() {
		return createdAttributes;
	}
	
	public void addCreatedAttribute(String uuid) {
		if (createdAttributes == null) {
			createdAttributes = new ArrayList<>();
		}
		createdAttributes.add(uuid);
	}
	
	public List<String> getMovedUsers() {
		return movedUsers;
	}
	
	public void addMovedUser(String uuid) {
		if (movedUsers == null) {
			movedUsers = new ArrayList<>();
		}
		movedUsers.add(uuid);
	}
	
	public String getPriorGender() {
		return priorGender;
	}
	
	public void setPriorGender(String priorGender) {
		this.priorGender = priorGender;
	}
	
	public Date getPriorDateOfBirth() {
		return priorDateOfBirth;
	}
	
	public void setPriorDateOfBirth(Date priorDateOfBirth) {
		this.priorDateOfBirth = priorDateOfBirth;
	}
	
	public boolean isPriorDateOfBirthEstimated() {
		return priorDateOfBirthEstimated;
	}
	
	public void setPriorDateOfBirthEstimated(boolean priorDateOfBirthEstimated) {
		this.priorDateOfBirthEstimated = priorDateOfBirthEstimated;
	}
	
	public Date getPriorDateOfDeath() {
		return priorDateOfDeath;
	}
	
	public void setPriorDateOfDeath(Date priorDateOfDeath) {
		this.priorDateOfDeath = priorDateOfDeath;
	}
	
	public Boolean getPriorDateOfDeathEstimated() {
		return priorDateOfDeathEstimated;
	}
	
	public void setPriorDateOfDeathEstimated(Boolean priorDateOfDeathEstimated) {
		this.priorDateOfDeathEstimated = priorDateOfDeathEstimated;
	}
	
	public String getPriorCauseOfDeath() {
		return priorCauseOfDeath;
	}
	
	public void setPriorCauseOfDeath(String uuid) {
		this.priorCauseOfDeath = uuid;
	}
	
	/**
	 * Computes a unique hash value representing the object
	 *
	 * @return hash value
	 */
	public int computeHashValue() {
		StringBuilder str = new StringBuilder();
		if (getCreatedAddresses() != null) {
			str.append(getCreatedAddresses().toString());
		}
		if (getCreatedAttributes() != null) {
			str.append(getCreatedAttributes().toString());
		}
		if (getCreatedIdentifiers() != null) {
			str.append(getCreatedIdentifiers().toString());
		}
		if (getCreatedNames() != null) {
			str.append(getCreatedNames().toString());
		}
		if (getCreatedOrders() != null) {
			str.append(getCreatedOrders().toString());
		}
		if (getCreatedPrograms() != null) {
			str.append(getCreatedPrograms().toString());
		}
		if (getMovedPrograms() != null) {
			str.append(getMovedPrograms().toString());
		}
		if (getCreatedRelationships() != null) {
			str.append(getCreatedRelationships().toString());
		}
		if (getVoidedRelationships() != null) {
			str.append(getVoidedRelationships().toString());
		}
		if (getMovedVisits() != null) {
			str.append(getMovedVisits().toString());
		}
		if (getMovedEncounters() != null) {
			str.append(getMovedEncounters().toString());
		}
		if (getMovedIndependentObservations() != null) {
			str.append(getMovedIndependentObservations().toString());
		}
		if (getMovedUsers() != null) {
			str.append(getMovedUsers().toString());
		}
		str.append(getPriorCauseOfDeath());
		str.append(getPriorGender());
		str.append((getPriorDateOfBirth() != null) ? getPriorDateOfBirth().toString() : getPriorDateOfBirth());
		str.append((getPriorDateOfBirth() != null) ? getPriorDateOfDeath().toString() : getPriorDateOfDeath());
		str.append(isPriorDateOfBirthEstimated());
		return str.toString().hashCode();
	}
	
}

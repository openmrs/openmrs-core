/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.parameter;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * A builder class for constructing instances of {@link VisitSearchCriteria}.
 * This builder allows for the flexible creation of {@link VisitSearchCriteria}
 * objects by providing a simple interface to set various fields.
 * 
 * @since 2.6.8
 * @since 2.7.0
 */
public class VisitSearchCriteriaBuilder {
	
	private Collection<VisitType> visitTypes;
	private Collection<Patient> patients;
	private Collection<Location> locations;
	private Collection<Concept> indications;
	private Date minStartDatetime;
	private Date maxStartDatetime;
	private Date minEndDatetime;
	private Date maxEndDatetime;
	private Map<VisitAttributeType, String> serializedAttributeValues;
	private boolean includeInactive = true;
	private boolean includeVoided = false;
	
	/**
	 * Constructs a new {@link VisitSearchCriteriaBuilder} instance.
	 */
	public VisitSearchCriteriaBuilder() {}
	
	/**
	 * Sets the visit types to include in the search criteria.
	 *
	 * @param visitTypes the collection of {@link VisitType} to include.
	 * @return the current instance of {@link VisitSearchCriteriaBuilder} for method chaining.
	 */
	public VisitSearchCriteriaBuilder visitTypes(Collection<VisitType> visitTypes) {
		this.visitTypes = visitTypes;
		return this;
	}
	
	/**
	 * Sets the patients to include in the search criteria.
	 *
	 * @param patients the collection of {@link Patient} to include.
	 * @return the current instance of {@link VisitSearchCriteriaBuilder} for method chaining.
	 */
	public VisitSearchCriteriaBuilder patients(Collection<Patient> patients) {
		this.patients = patients;
		return this;
	}
	
	/**
	 * Sets a single patient to include in the search criteria.
	 * 
	 * @param patient the {@link Patient} to include.
	 * @return the current instance of {@link VisitSearchCriteriaBuilder} for method chaining.
	 */
	public VisitSearchCriteriaBuilder patient(Patient patient){
		this.patients = Collections.singletonList(patient);
		return this;
	}
	
	/**
	 * Sets the locations to include in the search criteria.
	 *
	 * @param locations the collection of {@link Location} to include.
	 * @return the current instance of {@link VisitSearchCriteriaBuilder} for method chaining.
	 */
	public VisitSearchCriteriaBuilder locations(Collection<Location> locations) {
		this.locations = locations;
		return this;
	}
	
	/**
	 * Sets the indications to include in the search criteria.
	 *
	 * @param indications the collection of {@link Concept} to include.
	 * @return the current instance of {@link VisitSearchCriteriaBuilder} for method chaining.
	 */
	public VisitSearchCriteriaBuilder indications(Collection<Concept> indications) {
		this.indications = indications;
		return this;
	}
	
	/**
	 * Sets the minimum start datetime for visits to include in the search criteria.
	 *
	 * @param minStartDatetime the minimum start {@link Date}.
	 * @return the current instance of {@link VisitSearchCriteriaBuilder} for method chaining.
	 */
	public VisitSearchCriteriaBuilder minStartDatetime(Date minStartDatetime) {
		this.minStartDatetime = minStartDatetime;
		return this;
	}
	
	/**
	 * Sets the maximum start datetime for visits to include in the search criteria.
	 *
	 * @param maxStartDatetime the maximum start {@link Date}.
	 * @return the current instance of {@link VisitSearchCriteriaBuilder} for method chaining.
	 */
	public VisitSearchCriteriaBuilder maxStartDatetime(Date maxStartDatetime) {
		this.maxStartDatetime = maxStartDatetime;
		return this;
	}
	
	/**
	 * Sets the minimum end datetime for visits to include in the search criteria.
	 *
	 * @param minEndDatetime the minimum end {@link Date}.
	 * @return the current instance of {@link VisitSearchCriteriaBuilder} for method chaining.
	 */
	public VisitSearchCriteriaBuilder minEndDatetime(Date minEndDatetime) {
		this.minEndDatetime = minEndDatetime;
		return this;
	}
	
	/**
	 * Sets the maximum end datetime for visits to include in the search criteria.
	 *
	 * @param maxEndDatetime the maximum end {@link Date}.
	 * @return the current instance of {@link VisitSearchCriteriaBuilder} for method chaining.
	 */
	public VisitSearchCriteriaBuilder maxEndDatetime(Date maxEndDatetime) {
		this.maxEndDatetime = maxEndDatetime;
		return this;
	}
	
	/**
	 * Sets the serialized attribute values to filter visits by in the search criteria.
	 *
	 * @param serializedAttributeValues a map of {@link VisitAttributeType} to their corresponding serialized values.
	 * @return the current instance of {@link VisitSearchCriteriaBuilder} for method chaining.
	 */
	public VisitSearchCriteriaBuilder serializedAttributeValues(Map<VisitAttributeType, String> serializedAttributeValues) {
		this.serializedAttributeValues = serializedAttributeValues;
		return this;
	}
	
	/**
	 * Sets whether inactive visits should be included in the search criteria.
	 *
	 * @param includeInactive true to include inactive visits, false otherwise.
	 * @return the current instance of {@link VisitSearchCriteriaBuilder} for method chaining.
	 */
	public VisitSearchCriteriaBuilder includeInactive(boolean includeInactive) {
		this.includeInactive = includeInactive;
		return this;
	}
	
	/**
	 * Sets whether voided visits should be included in the search criteria.
	 *
	 * @param includeVoided true to include voided visits, false otherwise.
	 * @return the current instance of {@link VisitSearchCriteriaBuilder} for method chaining.
	 */
	public VisitSearchCriteriaBuilder includeVoided(boolean includeVoided) {
		this.includeVoided = includeVoided;
		return this;
	}
	
	/**
	 * Builds and returns a {@link VisitSearchCriteria} instance based on the current state of the builder.
	 *
	 * @return a new instance of {@link VisitSearchCriteria}.
	 */
	public VisitSearchCriteria build() {
		return new VisitSearchCriteria(visitTypes, patients, locations, indications, minStartDatetime, maxStartDatetime,
			minEndDatetime, maxEndDatetime, serializedAttributeValues, includeInactive, includeVoided);
	}
}

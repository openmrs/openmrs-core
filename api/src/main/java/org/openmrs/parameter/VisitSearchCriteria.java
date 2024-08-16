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
import java.util.Date;
import java.util.Map;

/**
 * The VisitSearchCriteria class encapsulates the criteria used for searching visits in the system.
 * It includes various fields that allow filtering visits based on different parameters such as visit type,
 * patient, location, and date ranges. It also provides options to include or exclude inactive and voided visits.
 * 
 * @since 2.6.8
 */
public class VisitSearchCriteria {
	
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
	private boolean includeVoided = true;
	
	/**
	 * Constructs a VisitSearchCriteria with the specified parameters. 
	 * Instead of calling this constructor directly, it is recommended to use {@link VisitSearchCriteriaBuilder}.
	 *
	 * @param visitTypes               the types of visits to include in the search
	 * @param patients                 the patients associated with the visits to include in the search
	 * @param locations                the locations associated with the visits to include in the search
	 * @param indications              the indications associated with the visits to include in the search
	 * @param minStartDatetime         the minimum start date of visits to include in the search
	 * @param maxStartDatetime         the maximum start date of visits to include in the search
	 * @param minEndDatetime           the minimum end date of visits to include in the search
	 * @param maxEndDatetime           the maximum end date of visits to include in the search
	 * @param serializedAttributeValues a map of serialized attribute values to filter visits by
	 * @param includeInactive          whether to include inactive visits in the search
	 * @param includeVoided            whether to include voided visits in the search
	 */
	public VisitSearchCriteria(Collection<VisitType> visitTypes, Collection<Patient> patients,
		Collection<Location> locations, Collection<Concept> indications,
		Date minStartDatetime, Date maxStartDatetime, Date minEndDatetime,
		Date maxEndDatetime, Map<VisitAttributeType, String> serializedAttributeValues,
		boolean includeInactive, boolean includeVoided) {
		this.visitTypes = visitTypes;
		this.patients = patients;
		this.locations = locations;
		this.indications = indications;
		this.minStartDatetime = minStartDatetime;
		this.maxStartDatetime = maxStartDatetime;
		this.minEndDatetime = minEndDatetime;
		this.maxEndDatetime = maxEndDatetime;
		this.serializedAttributeValues = serializedAttributeValues;
		this.includeInactive = includeInactive;
		this.includeVoided = includeVoided;
	}
	
	/**
	 * @return the collection of visit types included in the search criteria.
	 */
	public Collection<VisitType> getVisitTypes() {
		return visitTypes;
	}
	
	/**
	 * Sets the collection of visit types to be included in the search criteria.
	 *
	 * @param visitTypes the collection of visit types to set.
	 */
	public void setVisitTypes(Collection<VisitType> visitTypes) {
		this.visitTypes = visitTypes;
	}
	
	/**
	 * @return the collection of patients included in the search criteria.
	 */
	public Collection<Patient> getPatients() {
		return patients;
	}
	
	/**
	 * Sets the collection of patients to be included in the search criteria.
	 *
	 * @param patients the collection of patients to set.
	 */
	public void setPatients(Collection<Patient> patients) {
		this.patients = patients;
	}
	
	/**
	 * @return the collection of locations included in the search criteria.
	 */
	public Collection<Location> getLocations() {
		return locations;
	}
	
	/**
	 * Sets the collection of locations to be included in the search criteria.
	 *
	 * @param locations the collection of locations to set.
	 */
	public void setLocations(Collection<Location> locations) {
		this.locations = locations;
	}
	
	/**
	 * @return the collection of indications included in the search criteria.
	 */
	public Collection<Concept> getIndications() {
		return indications;
	}
	
	/**
	 * Sets the collection of indications to be included in the search criteria.
	 *
	 * @param indications the collection of indications to set.
	 */
	public void setIndications(Collection<Concept> indications) {
		this.indications = indications;
	}
	
	/**
	 * @return the minimum start datetime included in the search criteria.
	 */
	public Date getMinStartDatetime() {
		return minStartDatetime;
	}
	
	/**
	 * Sets the minimum start datetime to be included in the search criteria.
	 *
	 * @param minStartDatetime the minimum start datetime to set.
	 */
	public void setMinStartDatetime(Date minStartDatetime) {
		this.minStartDatetime = minStartDatetime;
	}
	
	/**
	 * @return the maximum start datetime included in the search criteria.
	 */
	public Date getMaxStartDatetime() {
		return maxStartDatetime;
	}
	
	/**
	 * Sets the maximum start datetime to be included in the search criteria.
	 *
	 * @param maxStartDatetime the maximum start datetime to set.
	 */
	public void setMaxStartDatetime(Date maxStartDatetime) {
		this.maxStartDatetime = maxStartDatetime;
	}
	
	/**
	 * @return the minimum end datetime included in the search criteria.
	 */
	public Date getMinEndDatetime() {
		return minEndDatetime;
	}
	
	/**
	 * Sets the minimum end datetime to be included in the search criteria.
	 *
	 * @param minEndDatetime the minimum end datetime to set.
	 */
	public void setMinEndDatetime(Date minEndDatetime) {
		this.minEndDatetime = minEndDatetime;
	}
	
	/**
	 * @return the maximum end datetime included in the search criteria.
	 */
	public Date getMaxEndDatetime() {
		return maxEndDatetime;
	}
	
	/**
	 * Sets the maximum end datetime to be included in the search criteria.
	 *
	 * @param maxEndDatetime the maximum end datetime to set.
	 */
	public void setMaxEndDatetime(Date maxEndDatetime) {
		this.maxEndDatetime = maxEndDatetime;
	}
	
	/**
	 * @return a map of serialized attribute values included in the search criteria.
	 */
	public Map<VisitAttributeType, String> getSerializedAttributeValues() {
		return serializedAttributeValues;
	}
	
	/**
	 * Sets the map of serialized attribute values to be included in the search criteria.
	 *
	 * @param serializedAttributeValues the map of serialized attribute values to set.
	 */
	public void setSerializedAttributeValues(Map<VisitAttributeType, String> serializedAttributeValues) {
		this.serializedAttributeValues = serializedAttributeValues;
	}
	
	/**
	 * @return true if inactive visits are included in the search criteria, false otherwise.
	 */
	public boolean isIncludeInactive() {
		return includeInactive;
	}
	
	/**
	 * Sets whether inactive visits should be included in the search criteria.
	 *
	 * @param includeInactive true to include inactive visits, false otherwise.
	 */
	public void setIncludeInactive(boolean includeInactive) {
		this.includeInactive = includeInactive;
	}
	
	/**
	 * @return true if voided visits are included in the search criteria, false otherwise.
	 */
	public boolean isIncludeVoided() {
		return includeVoided;
	}
	
	/**
	 * Sets whether voided visits should be included in the search criteria.
	 *
	 * @param includeVoided true to include voided visits, false otherwise.
	 */
	public void setIncludeVoided(boolean includeVoided) {
		this.includeVoided = includeVoided;
	}
}

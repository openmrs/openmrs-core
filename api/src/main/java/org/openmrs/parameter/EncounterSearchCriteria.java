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
package org.openmrs.parameter;

import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.VisitType;
import org.openmrs.Visit;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Date;

/**
 * The search parameter object for encounters
 *
 * @since 1.12
 */
public class EncounterSearchCriteria {
	
	private Patient patient;
	
	private Location location;
	
	private Date fromDate;
	
	private Date toDate;
	
	private Date dateChanged;
	
	private Collection<Form> enteredViaForms;
	
	private Collection<EncounterType> encounterTypes;
	
	private Collection<Provider> providers;
	
	private Collection<VisitType> visitTypes;
	
	private Collection<Visit> visits;
	
	private boolean includeVoided;
	
	/**
	 * @return the patient the encounter is for
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @param patient the patient the encounter is for
	 * @return this parameter object
	 */
	public EncounterSearchCriteria setPatient(@Nullable Patient patient) {
		this.patient = patient;
		return this;
	}
	
	/**
	 * @return the location this encounter took place
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * @param location the location this encounter took place
	 * @return this parameter object
	 */
	public EncounterSearchCriteria setLocation(@Nullable Location location) {
		this.location = location;
		return this;
	}
	
	/**
	 * @return the minimum date (inclusive) this encounter took place
	 */
	public Date getFromDate() {
		return fromDate;
	}
	
	/**
	 * @param fromDate the minimum date (inclusive) this encounter took place
	 * @return this parameter object
	 */
	public EncounterSearchCriteria setFromDate(@Nullable Date fromDate) {
		this.fromDate = fromDate;
		return this;
	}
	
	/**
	 * @return the maximum date (exclusive) this encounter took place
	 */
	public Date getToDate() {
		return toDate;
	}
	
	/**
	 * @param toDate toDate the maximum date (exclusive) this encounter took place
	 * @return this parameter object
	 */
	public EncounterSearchCriteria setToDate(@Nullable Date toDate) {
		this.toDate = toDate;
		return this;
	}
	
	/**
	 * @return the minimum date this encounter was changed
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged the minimum date this encounter was changed
	 * @return this parameter object
	 */
	public EncounterSearchCriteria setDateChanged(@Nullable Date dateChanged) {
		this.dateChanged = dateChanged;
		return this;
	}
	
	/**
	 * @return the form that entered this encounter must be in this list
	 */
	public Collection<Form> getEnteredViaForms() {
		return enteredViaForms;
	}
	
	/**
	 * @param enteredViaForms the form that entered the encounter must be in this collection.
	 *                           This search parameter is omitted if the set is null or empty.
	 * @return this parameter object
	 */
	public EncounterSearchCriteria setEnteredViaForms(@Nullable Collection<Form> enteredViaForms) {
		this.enteredViaForms = enteredViaForms;
		return this;
	}
	
	/**
	 * @return the type of encounter must be in this list
	 */
	public Collection<EncounterType> getEncounterTypes() {
		return encounterTypes;
	}
	
	/**
	 * @param encounterTypes the type of the encounter must be in this collection.
	 *                           This search parameter is omitted if the set is null or empty.
	 * @return this parameter object
	 */
	public EncounterSearchCriteria setEncounterTypes(@Nullable Collection<EncounterType> encounterTypes) {
		this.encounterTypes = encounterTypes;
		return this;
	}
	
	/**
	 * @return the provider of this encounter must be in this list
	 */
	public Collection<Provider> getProviders() {
		return providers;
	}
	
	/**
	 * @param providers the provider of the encounter must be in this collection.
	 *                           This search parameter is omitted if the set is null or empty.
	 * @return this parameter object
	 */
	public EncounterSearchCriteria setProviders(@Nullable Collection<Provider> providers) {
		this.providers = providers;
		return this;
	}
	
	/**
	 * @return the visit types of this encounter must be in this list
	 */
	public Collection<VisitType> getVisitTypes() {
		return visitTypes;
	}
	
	/**
	 * @param visitTypes the visit types of the encounter must be in this collection.
	 *                           This search parameter is omitted if the set is null or empty.
	 * @return this parameter object
	 */
	public EncounterSearchCriteria setVisitTypes(@Nullable Collection<VisitType> visitTypes) {
		this.visitTypes = visitTypes;
		return this;
	}
	
	/**
	 * @return the visits of this encounter must be in this list
	 */
	public Collection<Visit> getVisits() {
		return visits;
	}
	
	/**
	 * @param visits the visits of the encounter must be in this collection.
	 *                           This search parameter is omitted if the set is null or empty.
	 * @return this parameter object
	 */
	public EncounterSearchCriteria setVisits(@Nullable Collection<Visit> visits) {
		this.visits = visits;
		return this;
	}
	
	/**
	 * @return whether to include the voided encounters or not
	 */
	public boolean getIncludeVoided() {
		return includeVoided;
	}
	
	/**
	 * @param includeVoided whether to include the voided encounters or not
	 * @return this parameter object
	 */
	public EncounterSearchCriteria setIncludeVoided(boolean includeVoided) {
		this.includeVoided = includeVoided;
		return this;
	}
	
}

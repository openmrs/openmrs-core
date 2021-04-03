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

import java.util.Collection;
import java.util.Date;

import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.VisitType;

/**
 * The search parameter object for encounters. A convenience interface for building
 * instances is provided by {@link EncounterSearchCriteriaBuilder}.
 *
 * @since 1.12
 * @see EncounterSearchCriteriaBuilder
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
	 * Instead of calling this constructor directly, it is recommended to use {@link EncounterSearchCriteriaBuilder}.
	 * @param patient the patient the encounter is for
	 * @param location the location this encounter took place
	 * @param fromDate the minimum date (inclusive) the encounter took place
	 * @param toDate the maximum date (exclusive) the encounter took place
	 * @param dateChanged the minimum date the encounter was changed
	 * @param enteredViaForms the form that entered this encounter must be in this collection
	 * @param encounterTypes the type of the encounter must be in this collection
	 * @param providers the provider of the encounter must be in this collection
	 * @param visitTypes the visit types of the encounter must be in this collection
	 * @param visits the visits of the encounter must be in this collection
	 * @param includeVoided whether to include the voided encounters or not
	 */
	public EncounterSearchCriteria(Patient patient, Location location, Date fromDate, Date toDate, Date dateChanged,
								   Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes,
								   Collection<Provider> providers, Collection<VisitType> visitTypes,
								   Collection<Visit> visits, boolean includeVoided) {
		this.patient = patient;
		this.location = location;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.dateChanged = dateChanged;
		this.enteredViaForms = enteredViaForms;
		this.encounterTypes = encounterTypes;
		this.providers = providers;
		this.visitTypes = visitTypes;
		this.visits = visits;
		this.includeVoided = includeVoided;
	}

	/**
	 * @return the patient the encounter is for
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @return the location this encounter took place
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * @return the minimum date (inclusive) this encounter took place
	 */
	public Date getFromDate() {
		return fromDate;
	}

	/**
	 * @return the maximum date (exclusive) this encounter took place
	 */
	public Date getToDate() {
		return toDate;
	}

	/**
	 * @return the minimum date this encounter was changed
	 */
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @return the form that entered this encounter must be in this collection
	 */
	public Collection<Form> getEnteredViaForms() {
		return enteredViaForms;
	}

	/**
	 * @return the type of encounter must be in this list
	 */
	public Collection<EncounterType> getEncounterTypes() {
		return encounterTypes;
	}

	/**
	 * @return the provider of this encounter must be in this list
	 */
	public Collection<Provider> getProviders() {
		return providers;
	}

	/**
	 * @return the visit types of this encounter must be in this list
	 */
	public Collection<VisitType> getVisitTypes() {
		return visitTypes;
	}

	/**
	 * @return the visits of this encounter must be in this list
	 */
	public Collection<Visit> getVisits() {
		return visits;
	}

	/**
	 * @return whether to include the voided encounters or not
	 */
	public boolean getIncludeVoided() {
		return includeVoided;
	}
}

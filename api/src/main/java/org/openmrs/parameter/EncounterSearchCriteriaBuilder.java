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
 * A convenience builder for {@link EncounterSearchCriteria}. Create a builder, set
 * its properties to desired values and finally call {@link #createEncounterSearchCriteria()}
 * to create the actual search criteria instance.
 * @see EncounterSearchCriteria
 */
public class EncounterSearchCriteriaBuilder {
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
     * @param patient the patient the encounter is for
     * @return this builder instance
     */
    public EncounterSearchCriteriaBuilder setPatient(Patient patient) {
        this.patient = patient;
        return this;
    }

    /**
     * @param location the location the encounter took place
     * @return this builder instance
     */
    public EncounterSearchCriteriaBuilder setLocation(Location location) {
        this.location = location;
        return this;
    }

    /**
     * @param fromDate the minimum date (inclusive) the encounter took place
     * @return this builder instance
     */
    public EncounterSearchCriteriaBuilder setFromDate(Date fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    /**
     * @param toDate the maximum date (exclusive) the encounter took place
     * @return this builder instance
     */
    public EncounterSearchCriteriaBuilder setToDate(Date toDate) {
        this.toDate = toDate;
        return this;
    }

    /**
     * @param dateChanged the minimum date the encounter was changed
     * @return this builder instance
     */
    public EncounterSearchCriteriaBuilder setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
        return this;
    }

    /**
     * @param enteredViaForms the form that entered the encounter must be in this collection.
     *                           This search parameter is omitted if the set is null or empty.
     * @return this builder instance
     */
    public EncounterSearchCriteriaBuilder setEnteredViaForms(Collection<Form> enteredViaForms) {
        this.enteredViaForms = enteredViaForms;
        return this;
    }

    /**
     * @param encounterTypes the type of the encounter must be in this collection.
     *                           This search parameter is omitted if the set is null or empty.
     * @return this builder instance
     */
    public EncounterSearchCriteriaBuilder setEncounterTypes(Collection<EncounterType> encounterTypes) {
        this.encounterTypes = encounterTypes;
        return this;
    }

    /**
     * @param providers the provider of the encounter must be in this collection.
     *                           This search parameter is omitted if the set is null or empty.
     * @return this builder instance
     */
    public EncounterSearchCriteriaBuilder setProviders(Collection<Provider> providers) {
        this.providers = providers;
        return this;
    }

    /**
     * @param visitTypes the visit types of the encounter must be in this collection.
     *                           This search parameter is omitted if the set is null or empty.
     * @return this builder instance
     */
    public EncounterSearchCriteriaBuilder setVisitTypes(Collection<VisitType> visitTypes) {
        this.visitTypes = visitTypes;
        return this;
    }

    /**
     * @param visits the visits of the encounter must be in this collection.
     *                           This search parameter is omitted if the set is null or empty.
     * @return this builder instance
     */
    public EncounterSearchCriteriaBuilder setVisits(Collection<Visit> visits) {
        this.visits = visits;
        return this;
    }

    /**
     * @param includeVoided whether to include the voided encounters or not
     * @return this builder instance
     */
    public EncounterSearchCriteriaBuilder setIncludeVoided(boolean includeVoided) {
        this.includeVoided = includeVoided;
        return this;
    }

    /**
     * Create an {@link EncounterSearchCriteria} with the properties of this builder instance.
     * @return a new search criteria instance
     */
    public EncounterSearchCriteria createEncounterSearchCriteria() {
        return new EncounterSearchCriteria(patient, location, fromDate, toDate, dateChanged, enteredViaForms,
                encounterTypes, providers, visitTypes, visits, includeVoided);
    }
}
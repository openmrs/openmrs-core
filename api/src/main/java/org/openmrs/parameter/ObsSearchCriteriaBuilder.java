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

import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;

/**
 * A convenience builder for {@link ObsSearchCriteria}. Create a builder, set its properties to
 * desired values and finally call {@link #createObsSearchCriteria()} to create the actual search
 * criteria instance.
 *
 * @since 2.8.10
 * @see ObsSearchCriteria
 */
public class ObsSearchCriteriaBuilder {

	private List<Person> whom;

	private List<Encounter> encounters;

	private List<Concept> questions;

	private List<Concept> answers;

	private List<PERSON_TYPE> personTypes;

	private List<Location> locations;

	private List<String> sort;

	private List<Visit> visits;

	private Integer mostRecentN;

	private Integer obsGroupId;

	private Date fromDate;

	private Date toDate;

	private boolean includeVoidedObs;

	private String accessionNumber;

	private Integer startIndex;

	private Integer maxResults;

	/**
	 * @param whom List&lt;Person&gt; to restrict obs to
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setWhom(List<Person> whom) {
		this.whom = whom;
		return this;
	}

	/**
	 * @param encounters List&lt;Encounter&gt; to restrict obs to
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setEncounters(List<Encounter> encounters) {
		this.encounters = encounters;
		return this;
	}

	/**
	 * @param questions List&lt;Concept&gt; to restrict the obs to
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setQuestions(List<Concept> questions) {
		this.questions = questions;
		return this;
	}

	/**
	 * @param answers List&lt;Concept&gt; to restrict the valueCoded to
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setAnswers(List<Concept> answers) {
		this.answers = answers;
		return this;
	}

	/**
	 * @param personTypes List&lt;PERSON_TYPE&gt; objects to restrict the obs to, applied in addition to
	 *            any restriction {@link #setWhom(List)} imposes
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setPersonTypes(List<PERSON_TYPE> personTypes) {
		this.personTypes = personTypes;
		return this;
	}

	/**
	 * @param locations The org.openmrs.Location objects to restrict to
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setLocations(List<Location> locations) {
		this.locations = locations;
		return this;
	}

	/**
	 * @param sort list of column names to sort on (obsId, obsDatetime, etc), each optionally followed
	 *            by " asc"; if null or empty, defaults to obsDatetime descending
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setSort(List<String> sort) {
		this.sort = sort;
		return this;
	}

	/**
	 * @param visits List&lt;Visit&gt; to restrict obs to
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setVisits(List<Visit> visits) {
		this.visits = visits;
		return this;
	}

	/**
	 * @param mostRecentN restrict the number of obs returned to this size. This bound predates
	 *            {@link #setStartIndex(Integer)} and {@link #setMaxResults(Integer)} and takes
	 *            precedence over them, so setting it means the paging parameters are ignored
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setMostRecentN(Integer mostRecentN) {
		this.mostRecentN = mostRecentN;
		return this;
	}

	/**
	 * @param obsGroupId the Obs.getObsGroupId() to this integer
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setObsGroupId(Integer obsGroupId) {
		this.obsGroupId = obsGroupId;
		return this;
	}

	/**
	 * @param fromDate the earliest Obs date to get
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setFromDate(Date fromDate) {
		this.fromDate = fromDate;
		return this;
	}

	/**
	 * @param toDate the latest Obs date to get
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setToDate(Date toDate) {
		this.toDate = toDate;
		return this;
	}

	/**
	 * @param includeVoidedObs true/false whether to also include the voided obs
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setIncludeVoidedObs(boolean includeVoidedObs) {
		this.includeVoidedObs = includeVoidedObs;
		return this;
	}

	/**
	 * @param accessionNumber accession number
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
		return this;
	}

	/**
	 * @param startIndex the 0-based index of the first row to return. A negative value cannot be an
	 *            offset and is ignored, as is any value set alongside {@link #setMostRecentN(Integer)}
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
		return this;
	}

	/**
	 * @param maxResults the maximum number of rows to return. A value of zero or less cannot be a page
	 *            size and is ignored, meaning every matching observation is returned, as is any value
	 *            set alongside {@link #setMostRecentN(Integer)}
	 * @return this builder instance
	 */
	public ObsSearchCriteriaBuilder setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
		return this;
	}

	/**
	 * Create an {@link ObsSearchCriteria} with the properties of this builder instance.
	 *
	 * @return a new search criteria instance
	 */
	public ObsSearchCriteria createObsSearchCriteria() {
		return new ObsSearchCriteria(whom, encounters, questions, answers, personTypes, locations, sort, visits, mostRecentN,
		        obsGroupId, fromDate, toDate, includeVoidedObs, accessionNumber, startIndex, maxResults);
	}
}

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
 * The search parameter object for observations. A convenience interface for building instances is
 * provided by {@link ObsSearchCriteriaBuilder}.
 *
 * @since 2.8.10
 * @see ObsSearchCriteriaBuilder
 */
public class ObsSearchCriteria {

	private final List<Person> whom;

	private final List<Encounter> encounters;

	private final List<Concept> questions;

	private final List<Concept> answers;

	private final List<PERSON_TYPE> personTypes;

	private final List<Location> locations;

	private final List<String> sort;

	private final List<Visit> visits;

	private final Integer mostRecentN;

	private final Integer obsGroupId;

	private final Date fromDate;

	private final Date toDate;

	private final boolean includeVoidedObs;

	private final String accessionNumber;

	private final Integer startIndex;

	private final Integer maxResults;

	/**
	 * Instead of calling this constructor directly, it is recommended to use
	 * {@link ObsSearchCriteriaBuilder}.
	 *
	 * @param whom List&lt;Person&gt; to restrict obs to (optional)
	 * @param encounters List&lt;Encounter&gt; to restrict obs to (optional)
	 * @param questions List&lt;Concept&gt; to restrict the obs to (optional)
	 * @param answers List&lt;Concept&gt; to restrict the valueCoded to (optional)
	 * @param personTypes List&lt;PERSON_TYPE&gt; objects to restrict the obs to, applied in addition to
	 *            any restriction <code>whom</code> imposes (optional)
	 * @param locations The org.openmrs.Location objects to restrict to (optional)
	 * @param sort list of column names to sort on (obsId, obsDatetime, etc), each optionally followed
	 *            by " asc"; if null or empty, defaults to obsDatetime descending (optional)
	 * @param visits List&lt;Visit&gt; to restrict obs to (optional)
	 * @param mostRecentN restrict the number of obs returned to this size. This bound predates
	 *            <code>startIndex</code> and <code>maxResults</code> and takes precedence over them, so
	 *            setting it means the paging parameters are ignored (optional)
	 * @param obsGroupId the Obs.getObsGroupId() to this integer (optional)
	 * @param fromDate the earliest Obs date to get (optional)
	 * @param toDate the latest Obs date to get (optional)
	 * @param includeVoidedObs true/false whether to also include the voided obs (required)
	 * @param accessionNumber accession number (optional)
	 * @param startIndex the 0-based index of the first row to return; a negative value cannot be an
	 *            offset and is ignored (optional)
	 * @param maxResults the maximum number of rows to return; a value of zero or less cannot be a page
	 *            size and is ignored, meaning every matching observation is returned (optional)
	 */
	@SuppressWarnings("squid:S107")
	public ObsSearchCriteria(List<Person> whom, List<Encounter> encounters, List<Concept> questions, List<Concept> answers,
	    List<PERSON_TYPE> personTypes, List<Location> locations, List<String> sort, List<Visit> visits, Integer mostRecentN,
	    Integer obsGroupId, Date fromDate, Date toDate, boolean includeVoidedObs, String accessionNumber, Integer startIndex,
	    Integer maxResults) {
		this.whom = whom;
		this.encounters = encounters;
		this.questions = questions;
		this.answers = answers;
		this.personTypes = personTypes;
		this.locations = locations;
		this.sort = sort;
		this.visits = visits;
		this.mostRecentN = mostRecentN;
		this.obsGroupId = obsGroupId;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.includeVoidedObs = includeVoidedObs;
		this.accessionNumber = accessionNumber;
		this.startIndex = startIndex;
		this.maxResults = maxResults;
	}

	/**
	 * @return the List&lt;Person&gt; to restrict obs to
	 */
	public List<Person> getWhom() {
		return whom;
	}

	/**
	 * @return the List&lt;Encounter&gt; to restrict obs to
	 */
	public List<Encounter> getEncounters() {
		return encounters;
	}

	/**
	 * @return the List&lt;Concept&gt; to restrict the obs to
	 */
	public List<Concept> getQuestions() {
		return questions;
	}

	/**
	 * @return the List&lt;Concept&gt; to restrict the valueCoded to
	 */
	public List<Concept> getAnswers() {
		return answers;
	}

	/**
	 * @return the List&lt;PERSON_TYPE&gt; objects to restrict this to
	 */
	public List<PERSON_TYPE> getPersonTypes() {
		return personTypes;
	}

	/**
	 * @return the List&lt;Location&gt; objects to restrict to
	 */
	public List<Location> getLocations() {
		return locations;
	}

	/**
	 * @return the list of column names to sort on
	 */
	public List<String> getSort() {
		return sort;
	}

	/**
	 * @return the List&lt;Visit&gt; to restrict obs to
	 */
	public List<Visit> getVisits() {
		return visits;
	}

	/**
	 * @return the number of obs returned to this size
	 */
	public Integer getMostRecentN() {
		return mostRecentN;
	}

	/**
	 * @return the obsGroupId to restrict to
	 */
	public Integer getObsGroupId() {
		return obsGroupId;
	}

	/**
	 * @return the earliest Obs date to get
	 */
	public Date getFromDate() {
		return fromDate;
	}

	/**
	 * @return the latest Obs date to get
	 */
	public Date getToDate() {
		return toDate;
	}

	/**
	 * @return whether to include the voided obs or not
	 */
	public boolean getIncludeVoidedObs() {
		return includeVoidedObs;
	}

	/**
	 * @return whether to include the voided obs or not
	 */
	public boolean isIncludeVoidedObs() {
		return includeVoidedObs;
	}

	/**
	 * @return the accession number
	 */
	public String getAccessionNumber() {
		return accessionNumber;
	}

	/**
	 * @return the starting index of the result set
	 */
	public Integer getStartIndex() {
		return startIndex;
	}

	/**
	 * @return the maximum number of results to return
	 */
	public Integer getMaxResults() {
		return maxResults;
	}
}
